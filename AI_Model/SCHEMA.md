# Schéma données IA ↔ Java

**Lecture Ahmed en priorité.** Tu codes à partir de ce doc les records Java + les entités JPA.

## 1. JSON de sortie du LLM (imposé dans le prompt)

```json
{
  "nomPlat": "Poulet curry riz basmati",
  "ingredients": ["poulet", "riz basmati", "sauce curry", "oignons", "coriandre"],
  "portion": "moyen",
  "caloriesMin": 480,
  "caloriesMax": 620,
  "confiance": "moyenne"
}
```

### Règles

| Champ | Type | Contraintes |
|---|---|---|
| `nomPlat` | string | En français |
| `ingredients` | string[] | En français |
| `portion` | string enum | `"petit"` \| `"moyen"` \| `"grand"` |
| `caloriesMin` | int | > 0, < `caloriesMax` |
| `caloriesMax` | int | > 0, > `caloriesMin` |
| `confiance` | string enum | `"haute"` \| `"moyenne"` \| `"basse"` |

### Cas aucun plat visible

```json
{
  "nomPlat": "inconnu",
  "ingredients": [],
  "portion": "moyen",
  "caloriesMin": 0,
  "caloriesMax": 0,
  "confiance": "basse"
}
```

## 2. Record Java

Emplacement proposé : `src/main/java/fr/esgi/hla/itadaki/ai/AnalyseResult.java` (cf. `exemples/AnalyseResult.java` pour le code complet prêt à copier).

Version simple avec champs String (préférée pour POC 24h, pas de conversion enum nécessaire) :

```java
public record AnalyseResult(
    String nomPlat,
    List<String> ingredients,
    String portion,       // "petit" | "moyen" | "grand"
    int caloriesMin,
    int caloriesMax,
    String confiance      // "haute" | "moyenne" | "basse"
) {}
```

Spring AI décode automatiquement le JSON vers ce record via `BeanOutputConverter` :

```java
AnalyseResult result = chatClient.prompt(prompt).call().entity(AnalyseResult.class);
```

## 3. Entités JPA

Emplacement proposé : `src/main/java/fr/esgi/hla/itadaki/entity/`.

### User

```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;   // BCrypt encoded

    @Column(nullable = false)
    private String role = "USER";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
```

### Photo

```java
@Entity
public class Photo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User owner;

    @Column(nullable = false)
    private String path;            // ./uploads/{uuid}.jpg

    @Column(nullable = false)
    private String contentType;     // image/jpeg | image/png

    private long taille;

    @Column(nullable = false)
    private Instant uploadedAt = Instant.now();

    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Analyse> analyses = new ArrayList<>();
}
```

### Analyse

**Une ligne par passe (v1, v2, v3…).** Une seule `active=true` par Photo : c'est celle affichée à l'utilisateur dans l'UI.

```java
@Entity
public class Analyse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Photo photo;

    @Column(nullable = false)
    private int version;            // 1, 2, 3, ...

    @Column(nullable = false)
    private boolean active;         // true = passe affichée dans l'UI

    @Column(length = 2000)
    private String correctionUtilisateur;  // null pour v1, texte user pour v>1

    @Column(length = 4000, nullable = false)
    private String resultatJson;    // JSON brut sortie Ollama (archive)

    // Champs extraits pour requêtes SQL (agrégation calories/jour, filtres)
    @Column(nullable = false) private String nomPlat;
    @Column(nullable = false) private int caloriesMin;
    @Column(nullable = false) private int caloriesMax;
    @Column(nullable = false) private String portion;
    @Column(nullable = false) private String confiance;

    @Column(nullable = false) private String modeleUtilise;   // qwen2.5vl:7b
    private int tempsMs;                                       // latence inférence

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
```

## 4. DTOs pour API REST

Emplacement proposé : `src/main/java/fr/esgi/hla/itadaki/dto/`.

### AnalyseDto (réponse endpoint)

```java
public record AnalyseDto(
    Long id,
    int version,
    boolean active,
    String nomPlat,
    List<String> ingredients,
    String portion,
    int caloriesMin,
    int caloriesMax,
    String confiance,
    String correctionUtilisateur,  // null si v1
    Instant createdAt
) {}
```

### CorrectionRequest (body POST correction)

```java
public record CorrectionRequest(
    @NotBlank @Size(max = 500) String texteUtilisateur
) {}
```

### PhotoDto

```java
public record PhotoDto(
    Long id,
    String imageUrl,                 // /api/photos/{id}/image
    AnalyseDto analyseActive,
    Instant uploadedAt
) {}
```

### HistoriqueDto

```java
public record HistoriqueDto(
    List<PhotoDto> photos,
    List<CalorieParJour> caloriesParJour
) {}

public record CalorieParJour(
    LocalDate jour,
    int caloriesTotalesMin,
    int caloriesTotalesMax,
    int nombreRepas
) {}
```

### AuthDto

```java
public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 32) String username,
    @NotBlank @Size(min = 6, max = 128) String password
) {}

public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {}

public record AuthResponse(
    String token,        // JWT
    String username
) {}
```

## 5. Repos JPA

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Page<Photo> findByOwnerOrderByUploadedAtDesc(User owner, Pageable pageable);
}

public interface AnalyseRepository extends JpaRepository<Analyse, Long> {
    List<Analyse> findByPhotoOrderByVersionDesc(Photo photo);
    Optional<Analyse> findByPhotoAndActiveTrue(Photo photo);

    // Agrégation calories/jour
    @Query("""
        SELECT new fr.esgi.hla.itadaki.dto.CalorieParJour(
            CAST(a.createdAt AS LocalDate),
            SUM(a.caloriesMin),
            SUM(a.caloriesMax),
            COUNT(a)
        )
        FROM Analyse a
        WHERE a.photo.owner = :owner
          AND a.active = true
          AND a.createdAt >= :since
        GROUP BY CAST(a.createdAt AS LocalDate)
        ORDER BY CAST(a.createdAt AS LocalDate) DESC
        """)
    List<CalorieParJour> caloriesParJour(User owner, Instant since);
}
```

(La requête JPQL peut nécessiter un ajustement H2 si `CAST(Instant AS LocalDate)` n'est pas reconnu direct. Fallback : requête native SQL `CAST(created_at AS DATE)` avec timezone `Europe/Paris`.)

## 6. Structure packages recommandée

```
src/main/java/fr/esgi/hla/itadaki/
├── ItadakiApplication.java
├── ai/
│   ├── AnalyseResult.java
│   └── AnalyseService.java
├── config/
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── PhotoController.java
│   ├── AnalyseController.java
│   └── HistoriqueController.java
├── dto/
│   ├── AnalyseDto.java
│   ├── CorrectionRequest.java
│   ├── HistoriqueDto.java
│   ├── PhotoDto.java
│   ├── CalorieParJour.java
│   └── auth/{RegisterRequest, LoginRequest, AuthResponse}.java
├── entity/
│   ├── User.java
│   ├── Photo.java
│   └── Analyse.java
├── repository/
│   ├── UserRepository.java
│   ├── PhotoRepository.java
│   └── AnalyseRepository.java
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── service/
│   ├── PhotoService.java
│   ├── HistoriqueService.java
│   └── AuthService.java
└── exception/
    └── GlobalExceptionHandler.java
```
