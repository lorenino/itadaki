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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Creates 3 demo accounts with pre-filled meal history on startup.
 * Skipped if demo accounts already exist (idempotent on restart).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final MealPhotoRepository mealPhotoRepository;
    private final MealAnalysisRepository mealAnalysisRepository;
    private final PasswordEncoder passwordEncoder;

    record PlatInfo(String nom, int kcal, MealType type, String proteines, String glucides, String lipides) {}
    record CompteDemo(String email, String username, int platOffset) {}

    private static final List<PlatInfo> PLATS = List.of(
        new PlatInfo("Ramen tonkotsu",      650, MealType.LUNCH,     "30", "70", "22"),
        new PlatInfo("Sushi mix",            420, MealType.LUNCH,     "24", "60", "10"),
        new PlatInfo("Poké bowl saumon",     540, MealType.LUNCH,     "28", "55", "18"),
        new PlatInfo("Pad thaï",             580, MealType.DINNER,    "22", "78", "16"),
        new PlatInfo("Curry vert",           480, MealType.DINNER,    "18", "52", "20"),
        new PlatInfo("Salade cesar",         380, MealType.LUNCH,     "20", "24", "22"),
        new PlatInfo("Pizza margherita",     720, MealType.DINNER,    "28", "90", "26"),
        new PlatInfo("Bo bun",               510, MealType.LUNCH,     "26", "62", "12"),
        new PlatInfo("Tacos poulet",         630, MealType.DINNER,    "32", "68", "18"),
        new PlatInfo("Dahl lentilles",       420, MealType.DINNER,    "22", "58", "10"),
        new PlatInfo("Omelette champignons", 310, MealType.BREAKFAST, "22", "4",  "20"),
        new PlatInfo("Buddha bowl",          490, MealType.LUNCH,     "18", "60", "14")
    );

    private static final List<CompteDemo> COMPTES = List.of(
        new CompteDemo("kenji@itadaki.demo", "kenji", 0),
        new CompteDemo("yuki@itadaki.demo",  "yuki",  4),
        new CompteDemo("luca@itadaki.demo",  "luca",  8)
    );

    // Chemin avec slashs forward pour que le front resolve bien l'URL (Path.toString()
    // utilise \ sur Windows, ce qui casse la requete /uploads/demo/placeholder.jpg cote navigateur).
    private static final String DEMO_PHOTO_PATH = "./uploads/demo/placeholder.jpg";
    private static final Path DEMO_PHOTO = Path.of(DEMO_PHOTO_PATH);

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        // Assure que le placeholder demo existe sur disque (copie depuis le classpath
        // vers ./uploads/demo/placeholder.jpg si absent). Evite les 404 d'images
        // en demo et permet de re-analyser un meal seede via le flux Ollama.
        ensureDemoPhotoOnDisk();

        // Check idempotent par email : seed uniquement si les comptes demo sont absents.
        if (userRepository.existsByEmail("kenji@itadaki.demo")) {
            log.info("DataSeeder : comptes demo deja presents, seed ignore.");
            return;
        }

        log.info("DataSeeder : création des comptes démo et de l'historique pré-rempli...");

        String hashedPassword = passwordEncoder.encode("itadaki12345");
        LocalDateTime now = LocalDateTime.now();

        for (CompteDemo compte : COMPTES) {
            User user = createDemoUser(compte, hashedPassword);
            for (int i = 0; i < 4; i++) {
                PlatInfo plat = PLATS.get((compte.platOffset() + i) % PLATS.size());
                LocalDateTime uploadedAt = mealTimeFor(plat.type(), now.minusDays(4 - i));
                createMealEntry(user, plat, uploadedAt);
            }
        }

        log.info("DataSeeder : 3 comptes démo créés avec 4 repas chacun.");
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private User createDemoUser(CompteDemo compte, String hashedPassword) {
        User user = new User();
        user.setEmail(compte.email());
        user.setUsername(compte.username());
        user.setPasswordHash(hashedPassword);
        user.setRole(UserRole.USER);
        return userRepository.save(user);
    }

    private LocalDateTime mealTimeFor(MealType type, LocalDateTime base) {
        return switch (type) {
            case BREAKFAST -> base.withHour(8).withMinute(0);
            case LUNCH     -> base.withHour(12).withMinute(30);
            case SNACK     -> base.withHour(16).withMinute(0);
            case DINNER    -> base.withHour(19).withMinute(30);
        };
    }

    private void createMealEntry(User user, PlatInfo plat, LocalDateTime uploadedAt) {
        Meal meal = new Meal();
        meal.setUser(user);
        meal.setStatus(MealStatus.ANALYSED);
        meal.setMealType(plat.type());
        meal = mealRepository.save(meal);
        // Override @CreationTimestamp via direct query to set the historical date.
        mealRepository.updateUploadedAt(meal.getId(), uploadedAt);

        // Toutes les photos demo pointent vers le meme placeholder reel.
        // OriginalFileName garde le nom du plat pour le contexte humain / admin panel.
        String nomFichier = plat.nom().replace(" ", "-") + ".jpg";
        MealPhoto photo = new MealPhoto();
        photo.setMeal(meal);
        photo.setOriginalFileName(nomFichier);
        photo.setFileName("placeholder.jpg");
        photo.setStoragePath(DEMO_PHOTO_PATH);
        photo.setContentType("image/jpeg");
        photo.setSize(184_392L);
        mealPhotoRepository.save(photo);

        int kcalMin = (int) (plat.kcal() * 0.85);
        int kcalMax = (int) (plat.kcal() * 1.15);
        String rawJson = """
                {"nomPlat":"%s","ingredients":[{"nom":"%s","caloriesApprox":%d,"proteines":%s,"glucides":%s,"lipides":%s}],"portion":"moyen","caloriesMin":%d,"caloriesMax":%d,"confiance":"haute"}
                """.formatted(
                plat.nom(), plat.nom(), plat.kcal(),
                plat.proteines(), plat.glucides(), plat.lipides(),
                kcalMin, kcalMax
        ).strip();

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

    private void ensureDemoPhotoOnDisk() {
        if (Files.exists(DEMO_PHOTO)) return;
        try {
            Files.createDirectories(DEMO_PHOTO.getParent());
            ClassPathResource resource = new ClassPathResource("demo/placeholder.jpg");
            if (!resource.exists()) {
                log.warn("DataSeeder : classpath:/demo/placeholder.jpg introuvable, photos demo afficheront le Dish SVG de fallback.");
                return;
            }
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, DEMO_PHOTO, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("DataSeeder : placeholder demo copie vers {}", DEMO_PHOTO);
        } catch (IOException ex) {
            log.warn("DataSeeder : impossible d'ecrire le placeholder demo ({})", ex.getMessage());
        }
    }
}
