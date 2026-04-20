package fr.esgi.hla.itadaki.initialisation;

import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.business.MealAnalysis;
import fr.esgi.hla.itadaki.business.MealPhoto;
import fr.esgi.hla.itadaki.business.User;
import fr.esgi.hla.itadaki.business.enums.MealStatus;
import fr.esgi.hla.itadaki.business.enums.MealType;
import fr.esgi.hla.itadaki.business.enums.UserRole;
import fr.esgi.hla.itadaki.repository.MealAnalysisRepository;
import fr.esgi.hla.itadaki.repository.MealPhotoRepository;
import fr.esgi.hla.itadaki.repository.MealRepository;
import fr.esgi.hla.itadaki.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Crée 3 comptes démo avec historique pré-rempli au démarrage de l'application.
 * Le seed est ignoré si des utilisateurs non-admin existent déjà (évite le double-seed
 * au restart DevTools).
 */
@Component
@AllArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final MealPhotoRepository mealPhotoRepository;
    private final MealAnalysisRepository mealAnalysisRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        // Check idempotent par email : on seed UNIQUEMENT les comptes démo
        // absents. Ne depend pas du count global (permet de seeder meme quand
        // d'autres utilisateurs de test existent deja).
        if (userRepository.existsByEmail("kenji@itadaki.demo")) {
            log.info("DataSeeder : comptes demo deja presents, seed ignore.");
            return;
        }

        log.info("DataSeeder : création des comptes démo et de l'historique pré-rempli...");

        // ── Plats disponibles ────────────────────────────────────────────────
        record PlatInfo(String nom, int kcal, MealType type, String proteines, String glucides, String lipides) {}

        List<PlatInfo> plats = List.of(
            new PlatInfo("Ramen tonkotsu",     650, MealType.LUNCH,     "30", "70", "22"),
            new PlatInfo("Sushi mix",           420, MealType.LUNCH,     "24", "60", "10"),
            new PlatInfo("Poké bowl saumon",    540, MealType.LUNCH,     "28", "55", "18"),
            new PlatInfo("Pad thaï",            580, MealType.DINNER,    "22", "78", "16"),
            new PlatInfo("Curry vert",          480, MealType.DINNER,    "18", "52", "20"),
            new PlatInfo("Salade cesar",        380, MealType.LUNCH,     "20", "24", "22"),
            new PlatInfo("Pizza margherita",    720, MealType.DINNER,    "28", "90", "26"),
            new PlatInfo("Bo bun",              510, MealType.LUNCH,     "26", "62", "12"),
            new PlatInfo("Tacos poulet",        630, MealType.DINNER,    "32", "68", "18"),
            new PlatInfo("Dahl lentilles",      420, MealType.DINNER,    "22", "58", "10"),
            new PlatInfo("Omelette champignons",310, MealType.BREAKFAST, "22", "4",  "20"),
            new PlatInfo("Buddha bowl",         490, MealType.LUNCH,     "18", "60", "14")
        );

        // ── Comptes démo ─────────────────────────────────────────────────────
        record CompteDemo(String email, String username, int platOffset) {}
        List<CompteDemo> comptes = List.of(
            new CompteDemo("kenji@itadaki.demo", "kenji", 0),
            new CompteDemo("yuki@itadaki.demo",  "yuki",  4),
            new CompteDemo("luca@itadaki.demo",  "luca",  8)
        );

        String motDePasse = passwordEncoder.encode("itadaki12345");
        LocalDateTime now = LocalDateTime.now();

        for (CompteDemo compte : comptes) {
            // Créer l'utilisateur
            User user = new User();
            user.setEmail(compte.email());
            user.setUsername(compte.username());
            user.setPasswordHash(motDePasse);
            user.setRole(UserRole.USER);
            user = userRepository.save(user);

            // Créer 4 meals sur les 5 derniers jours
            for (int i = 0; i < 4; i++) {
                PlatInfo plat = plats.get((compte.platOffset() + i) % plats.size());
                LocalDateTime uploadedAt = now.minusDays(4 - i);

                // Ajuster l'heure selon le type de repas
                uploadedAt = switch (plat.type()) {
                    case BREAKFAST -> uploadedAt.withHour(8).withMinute(0);
                    case LUNCH     -> uploadedAt.withHour(12).withMinute(30);
                    case SNACK     -> uploadedAt.withHour(16).withMinute(0);
                    case DINNER    -> uploadedAt.withHour(19).withMinute(30);
                };

                // ── Meal ────────────────────────────────────────────────────
                Meal meal = new Meal();
                meal.setUser(user);
                meal.setStatus(MealStatus.ANALYSED);
                meal.setMealType(plat.type());
                meal = mealRepository.save(meal);

                // Forcer uploadedAt après le save (CreationTimestamp gère la colonne)
                // via une requête directe pour contourner @CreationTimestamp
                mealRepository.updateUploadedAt(meal.getId(), uploadedAt);

                // ── MealPhoto ────────────────────────────────────────────────
                String nomFichier = plat.nom().replace(" ", "-") + ".jpg";
                MealPhoto photo = new MealPhoto();
                photo.setMeal(meal);
                photo.setOriginalFileName(nomFichier);
                photo.setFileName(nomFichier);
                photo.setStoragePath("./uploads/demo/" + nomFichier);
                photo.setContentType("image/jpeg");
                photo.setSize(500_000L);
                mealPhotoRepository.save(photo);

                // ── JSON factice cohérent ────────────────────────────────────
                int kcalMin = (int) (plat.kcal() * 0.85);
                int kcalMax = (int) (plat.kcal() * 1.15);
                String rawJson = """
                        {"nomPlat":"%s","ingredients":[{"nom":"%s","caloriesApprox":%d,"proteines":%s,"glucides":%s,"lipides":%s}],"portion":"moyen","caloriesMin":%d,"caloriesMax":%d,"confiance":"haute"}
                        """.formatted(
                        plat.nom(), plat.nom(), plat.kcal(),
                        plat.proteines(), plat.glucides(), plat.lipides(),
                        kcalMin, kcalMax
                ).strip();

                // ── MealAnalysis ─────────────────────────────────────────────
                MealAnalysis analysis = new MealAnalysis();
                analysis.setMeal(meal);
                analysis.setDetectedDishName(plat.nom());
                analysis.setEstimatedTotalCalories((double) plat.kcal());
                analysis.setConfidenceScore(0.85);
                analysis.setRawModelResponse(rawJson);
                analysis.setDetectedItemsJson(rawJson);
                analysis.setAnalyzedAt(uploadedAt);
                mealAnalysisRepository.save(analysis);
            }
        }

        log.info("DataSeeder : 3 comptes démo créés avec 4 repas chacun.");
    }
}
