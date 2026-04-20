# Flow request/response Itadaki

4 endpoints principaux côté REST + 1 utilitaire image.

## Endpoints

| Méthode | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | public | Inscription (BF1) |
| POST | `/api/auth/login` | public | Connexion (BF2) |
| POST | `/api/auth/logout` | auth | Déconnexion (BF5) |
| POST | `/api/photos` | auth | Upload photo (multipart) |
| POST | `/api/photos/{id}/analyses` | auth | Déclenche analyse v1 (BF3 part 1) |
| POST | `/api/analyses/{id}/corrections` | auth | 2ᵉ passe avec correction user (BF3 part 2) |
| GET | `/api/photos/{id}/image` | auth | Stream JPEG (pour Thymeleaf) |
| GET | `/api/historique` | auth | Liste + agrégation calories/jour (BF4) |

## 1. Upload photo

```
Client (Thymeleaf form) → POST /api/photos
  Content-Type: multipart/form-data
  file: <photo.jpg>
    │
    ▼
PhotoController.upload(MultipartFile file, Authentication auth)
    │
    ▼
PhotoService.upload(file, owner)
    ├─ UUID + extension → path = "{app.uploads.dir}/{uuid}.jpg"
    ├─ Files.createDirectories(parent)
    ├─ file.transferTo(path)
    └─ Photo saved = photoRepo.save(new Photo(owner, path, contentType, size))
    │
    ▼
Response 201 Created : PhotoDto { id, uploadedAt, imageUrl }
```

## 2. Analyse initiale (v1)

```
Client → POST /api/photos/42/analyses
    │
    ▼
AnalyseController.analyser(photoId=42, auth)
    ├─ vérif ownership
    └─ AnalyseService.analyserInitiale(photo)
        ├─ bytes = Files.readAllBytes(photo.path)
        ├─ media = new Media(MimeType, new ByteArrayResource(bytes))
        ├─ userMsg = UserMessage.builder()
        │     .text("Identifie ce plat…")
        │     .media(media)
        │     .build()
        ├─ TRY : result = chatClient.prompt(new Prompt(userMsg))
        │     .call()
        │     .entity(AnalyseResult.class)
        ├─ CATCH : 1 retry avec prompt STRICT MODE
        ├─ CATCH 2 : AnalyseResult.inconnu()
        ├─ analyse = new Analyse(
        │       photo, version=1, active=true,
        │       resultatJson=serialize(result),
        │       nomPlat=result.nomPlat(),
        │       caloriesMin=result.caloriesMin(), caloriesMax=result.caloriesMax(),
        │       portion=result.portion(), confiance=result.confiance(),
        │       modeleUtilise="qwen2.5vl:7b",
        │       tempsMs=chrono)
        └─ analyseRepo.save(analyse)
    │
    ▼
Response 200 OK : AnalyseDto
```

## 3. Correction (v2+)

```
Client → POST /api/analyses/99/corrections
  body: { "texteUtilisateur": "Ce n'est pas du poulet, c'est du tofu" }
    │
    ▼
AnalyseController.corriger(analyseId=99, CorrectionRequest req, auth)
    ├─ vérif ownership (via photo.owner)
    └─ AnalyseService.corriger(lastAnalyse, texte)
        ├─ photo = lastAnalyse.photo
        ├─ bytes = Files.readAllBytes(photo.path)
        ├─ promptV2 = """
        │       You previously produced: {lastAnalyse.resultatJson}
        │       User corrects with: {texte}
        │       Re-analyze with this correction. Return JSON only.
        │       """
        ├─ userMsg = UserMessage.builder().text(promptV2).media(media).build()
        ├─ result = chatClient.prompt(...).call().entity(AnalyseResult.class)
        ├─ lastAnalyse.setActive(false)     // désactive la précédente
        ├─ newAnalyse = new Analyse(
        │       photo, version=lastAnalyse.version+1, active=true,
        │       correctionUtilisateur=texte,
        │       resultatJson=serialize(result), champs extraits, …)
        └─ analyseRepo.saveAll(List.of(lastAnalyse, newAnalyse))
    │
    ▼
Response 200 OK : AnalyseDto (nouvelle version)
```

**Règle clé** : une seule Analyse `active=true` par Photo à un instant donné. Toutes les versions sont conservées (E16 : garder toutes les passes).

## 4. Stream image (pour Thymeleaf)

```
Client <img src="/api/photos/42/image">
    │
    ▼
PhotoController.image(photoId=42, auth)
    ├─ vérif ownership
    ├─ path = photoRepo.findById(42).getPath()
    └─ return ResponseEntity.ok()
           .contentType(MediaType.valueOf(photo.contentType))
           .body(new FileSystemResource(path))
```

## 5. Historique

```
Client → GET /api/historique?page=0&size=20
    │
    ▼
HistoriqueController.historique(auth, page=0, size=20)
    │
    ▼
HistoriqueService.lister(owner, page, size)
    ├─ photosPage = photoRepo.findByOwnerOrderByUploadedAtDesc(owner, Pageable.of(page, size))
    ├─ For each photo : fetch analyse active (AnalyseRepo.findByPhotoAndActiveTrue)
    ├─ Map → PhotoDto { id, imageUrl="/api/photos/{id}/image", analyseActive, uploadedAt }
    ├─ since7j = Instant.now().minus(7, DAYS)
    ├─ caloriesParJour = analyseRepo.caloriesParJour(owner, since7j)
    └─ return new HistoriqueDto(photos, caloriesParJour)
    │
    ▼
Response 200 OK : HistoriqueDto
```

## 6. Auth JWT

```
POST /api/auth/register
  body: { username, password }
    │
    ▼
AuthController.register(req)
    ├─ vérif username unique
    ├─ passwordEncoded = bcrypt.encode(password)
    ├─ User saved = userRepo.save(new User(username, passwordEncoded, "USER"))
    └─ token = jwtUtil.generate(username)
    │
    ▼
Response 201 : { token, username }

POST /api/auth/login
  body: { username, password }
    │
    ▼
AuthController.login(req)
    ├─ authManager.authenticate(UsernamePasswordAuthToken(username, password))
    └─ token = jwtUtil.generate(username)
    │
    ▼
Response 200 : { token, username }

Subsequent requests :
  Header: Authorization: Bearer <token>
  → JwtAuthenticationFilter extrait + valide + set SecurityContext
```

## Gestion d'erreur Ollama

Dans `AnalyseService` :

| Cas | Action |
|---|---|
| JSON invalide (parse échec) | 1 retry avec prompt STRICT MODE |
| 2ᵉ retry échoue | Renvoyer `AnalyseResult.inconnu()` + warn log + HTTP 503 UX FR |
| Connexion Ollama refusée | HTTP 503 « Service IA indisponible » + plan B seeds démo |
| Timeout (> 180s) | HTTP 504 « analyse trop longue, réessayez » |

Implémentation via `@ControllerAdvice` + classes d'exception custom (`AnalyseJsonInvalideException`, `OllamaIndisponibleException`).
