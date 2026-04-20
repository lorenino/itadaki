# Itadaki — Décisions à trancher (équipe)

Fichier de travail pour trancher en équipe (**Lorenzo**, **Hugo**, **Ahmed**).
Remplis au fil de l'eau. Une fois tranché, je persiste les choix dans `../wiki/projets/itadaki.md` et on attaque le plan 24h.

**Priorité** : sections **A**, **B**, **H** débloquent tout le reste (archi Ahmed, modèle Hugo/Lorenzo, répartition).

**Conventions de remplissage** :
- Coche `[x]` devant l'option retenue
- Ou écris libre après `→ Réponse :`
- Laisse vide si pas encore tranché

---

## A. Modèle Ollama & machine de démo

### A1. Machine de démo
GPU (modèle + VRAM) ou CPU-only ? RAM totale ?
→ Réponse : **PC Hugo** (RTX 5060, 32 Go RAM). IP LAN Ollama : **http://10.213.203.128:11434** (validé 2026-04-20 12:30, voir `AI_Model/VALIDATION-OLLAMA.md`).

### A2. Un seul modèle ou benchmark en parallèle ?
- [x] Commit direct sur `qwen2.5vl:7b` (reco agent)
- [ ] Benchmark rapide `qwen2.5vl:7b` + `gemma3:4b` (~1h data scientist) puis trancher
- [ ] Autre :

→ Réponse : **`qwen2.5vl:7b` acté** (2026-04-20 12:30). Test image réel validé : JSON strict parfait du 1er coup, FR natif, 6 ingrédients détectés, toutes les règles du schéma respectées. `gemma3:4b` pullé comme fallback mais non benchmarké (latence 64s = cold start non-bloquante pour POC, plan B K7 de toute façon).

### A3. Langue de sortie du LLM
- [ ] FR (imposé dans prompt, meilleur pour démo)
- [x] EN (plus fiable sur certains modèles)

→ Réponse :

### A4. Pre-pull des modèles ce soir (~7 Go + 3 Go)
Qui ? Sur quelle machine ?
→ Réponse : Hugo 32 GRAM et rtx5060

---

## B. Stratégie calories

### B5. Approche principale
- [x] **LLM direct** : le modèle renvoie le chiffre (simple, ±40%)
- [ ] **Hybride LLM → CIQUAL ANSES** (crédible, +4-6h intégration fuzzy match FR)
- [ ] **Hybride LLM → USDA FoodData Central** (API, libellés EN)
- [ ] Autre :

→ Réponse : LLM direct + fourchette B7 + disclaimer UX (choisi par Claude le 2026-04-20). Justification : CIQUAL = 4-6h de fuzzy match FR non budgétables sur 24h à 3. L'incertitude est couverte par la correction utilisateur (BF3 2ᵉ passe du sujet). Plan B si Hugo a du temps à H+18 : pré-charger 20 plats FR fréquents en mémoire pour booster la fiabilité.

### B6. Format portion demandé au LLM
- [ ] Grammes précis (risque d'hallucination)
- [x] Catégorie qualitative (petit / moyen / grand) + traduction côté serveur
- [ ] Les deux (LLM propose grammes, backend recalcule avec table)

→ Réponse :

### B7. Affichage calories UI
- [ ] Valeur unique ("450 kcal")
- [x] Fourchette honnête ("400-550 kcal")
- [ ] Avec niveau de confiance (haute/moyenne/basse)

→ Réponse :

### B8. Macros (protéines / glucides / lipides) en plus des calories ?
- [ ] Calories seulement (scope minimal)
- [x] Calories + macros (+20% effort, +30% impact jury)

→ Réponse :mets macros cest pour plus tard dabord on fait calories et on valide la totalité 

---

## C. Auth & utilisateurs

### C9. Mode d'authentification
- [ ] Session + HTTP Basic (simple, Swagger UI natif, reco)
- [x] JWT (plus "pro", +2-3h boilerplate)

→ Réponse : on utilise spring secu

### C10. Création de comptes
- [ ] Inscription libre (n'importe qui s'enregistre)
- [ ] Comptes pré-seedés (3 users hardcodés pour démo)
- [x] Les deux

→ Réponse : les deux (corrigé 2026-04-20 après audit). Raison : comptes pré-seedés seuls violaient BF1 « S'inscrire ». Endpoint `/api/auth/register` obligatoire + 3 seeds pour accélérer la démo.

### C11. Rôles
- [x] `USER` seul
- [ ] `USER` + `ADMIN` (admin voit tous les historiques)

→ Réponse : comme demandé dans sujet 

---

## D. Frontend

### D12. Choix UI
- [ ] **a.** Swagger UI only (0h — démo sur Swagger)
- [x] **b.** Thymeleaf + PicoCSS/Bootstrap (6-10h — pages login/upload/résultat/historique) **← reco**
- [ ] **c.** SPA React/Vue externe (15h+, risqué à 3)

→ Réponse :

### D13. Si Thymeleaf : upload photo
- [ ] Drag-drop HTML5
- [ ] Simple `<input type="file">`
- [x] Les deux

→ Réponse :

### D14. Page résultat — on affiche quoi ?
- [x] Image uploadée
- [x] Nom du plat
- [x] Liste des ingrédients détectés
- [x] Calories (valeur ou fourchette)
- [ ] Macros (si B8 coché)
- [x] Bouton "corriger"

→ Tout / sélection : image + nom + ingrédients + calories + corriger. Décoché Macros (cohérent avec B8 « plus tard »). Ajouté Image uploadée (cohérent BF4 « historique des photos téléversées »). Corrigé 2026-04-20.

---

## E. Correction utilisateur (2ᵉ passe — besoin fonctionnel #4)

### E15. Gestion du contexte
- [ ] ChatMemory Spring AI (garde tout le dialogue, plus impressionnant)
- [x] Reconstruction stateless (plus robuste, plus simple, reco agent)

→ Réponse : reconstruction stateless (choisi par Claude le 2026-04-20). Justification : cohérent avec E16 (garder toutes les passes en DB), plus robuste à debug, facile à afficher en timeline UI, et on peut parfaitement conserver l'historique sans ChatMemory (une ligne Analyse par passe, liée à la même Photo).

### E16. Historique des passes
- [x] Garder toutes les passes (analyse v1, v2, v3...)
- [ ] Écraser à chaque correction

→ Réponse : on garde toutes les passes (décidé 2026-04-20). L'utilisateur peut choisir laquelle est "active" dans l'UI (champ `active=true/false` sur Analyse) mais les versions précédentes restent en DB — traçabilité + démo plus riche.

### E17. Nombre max de corrections
- [ ] 1 seule
- [x] Illimité
- [ ] Limité à N (préciser N)

→ Réponse :

---

## F. Historique & suivi consommation

### F18. Niveau d'historique
- [x] Minimum : liste chronologique photos + analyses
- [x] + graphique calories/jour (7 derniers jours)
- [ ] + objectif journalier personnalisé
- [ ] + streak / gamification

→ Réponse : demander dans la cosnigne , on prevoit une version POC Puis on ajoutera desfeatures apres mais il faut un objectif fonctionnel 

### F19. Filtres / recherche
- [x] Juste liste descendante
- [ ] Filtre par date
- [ ] Recherche texte plat

→ Réponse :

---

## G. Scope — in / out (figer MAINTENANT)

| # | Feature | In | Out |
|---|---------|----|----|
| G20 | Objectif calorique journalier personnalisé | [ ] | [ ] |
| G21 | Graphique évolution 7 jours | [ ] | [ ] |
| G22 | Export CSV / PDF de l'historique | [ ] | [ ] |
| G23 | OAuth Google / GitHub | [ ] | [ ] |
| G24 | Mode mobile / responsive | [ ] | [ ] |

Commentaires :
→

---

## H. Équipe 3 — répartition après l'archi Maven d'Ahmed

### H25. Qui fait le data scientist (Ollama, prompts, benchmarks, calories) ?
- [x] Lorenzo
- [x] Hugo
- [ ] Ahmed

→ Réponse :on separe mais si on peut tout faire ii cest ok 

### H26. Qui fait le frontend (Thymeleaf ou autre) ?
- [x] Lorenzo
- [x] Hugo
- [x] Ahmed

→ Réponse :

### H27. Qui fait le contrôleur + service d'analyse (intégration Spring AI) ?
- [x] Lorenzo
- [x] Hugo
- [x] Ahmed

→ Réponse :

### H28. Ahmed après l'init Maven
- [x] Continue sur Spring Security + JPA
- [x] Libère et bascule sur frontend ou autre
- [x] Autre :

→ Réponse :on sen fout la en vrai on va juste avncer 

---

## I. Démo — logistique

### I29. Heure précise de la démo (mardi 21 avril après-midi) ?
→ Réponse : je sais pas 

### I30. Format démo
- [x] Vidéo pré-enregistrée 3 min + tentative live si stable (reco)
- [ ] Full live uniquement
- [ ] Full vidéo uniquement

→ Réponse :

### I31. Orateurs
- [x] Lorenzo
- [x] Hugo
- [x] Ahmed

(Reco : max 2 voix pour 15 min)
→ Réponse :

### I32. Machine qui fait tourner Ollama en démo
- [ ] Lorenzo
- [x] Hugo
- [ ] Ahmed

→ Réponse :

### I33. Dépendance réseau
- [x] Tout en local (recommandé, aucun risque WiFi)
- [ ] WiFi salle autorisé (pour APIs externes ?)

→ Réponse :

---

## J. Repo / workflow git

### J34. Branch model
- [ ] `main` direct
- [x] `main` + `feat/*` avec PR
- [ ] `main` + `dev` + `feat/*`

→ Réponse :

### J35. Qui invite `efikusu` sur https://github.com/lorenino/itadaki/settings/access (à faire CE SOIR) ?
→ Réponse : moi

### J36. Convention commits
- [x] Conventional Commits (`feat:`, `fix:`, `chore:`)
- [ ] Libre

→ Réponse :

### J37. Langue README
- [x] FR
- [ ] EN

→ Réponse :

---

## K. Paramètres techniques arrêtés (2026-04-20 après audit)

Ces paramètres sont tranchés par défaut sur la base des recommandations agents + audit conformité. Modifiables si besoin.

| # | Paramètre | Valeur arrêtée | Où le poser |
|---|-----------|----------------|-------------|
| K1 | Stockage photos | Disque `./uploads/{uuid}.jpg` + path en DB | `PhotoService` + `application.properties` (`app.uploads.dir`) |
| K2 | `spring.servlet.multipart.max-file-size` | `10MB` | `application.properties` |
| K3 | `spring.servlet.multipart.max-request-size` | `12MB` | `application.properties` |
| K4 | Parsing JSON Ollama invalide | Try/catch + 1 retry avec prompt renforcé « REPLY JSON ONLY », sinon message UX « analyse impossible » | `AnalyseService` |
| K5 | Timezone historique | `Europe/Paris` explicite (agrégation calories/jour) | Jackson `spring.jackson.time-zone` + logique service |
| K6 | Pagination historique | 20 derniers par défaut (`Pageable`) | `HistoriqueController` |
| K7 | Plan B Ollama mort en démo | 3 analyses pré-seedées en DB + vidéo 3 min filmée la veille | `DataSeeder` + asset `demo-backup.mp4` |
| K8 | `OLLAMA_HOST` | **Machine Hugo validée : `http://10.213.203.128:11434`** (2026-04-20). Ouvert LAN, firewall OK | `application.properties` (`spring.ai.ollama.base-url`) |

## L. À confirmer (externe ou équipe)

- **A1** Machine de démo définitive (celle d'Hugo probablement, à fixer)
- **I29** Horaire précis de la démo — à demander au prof
- **Nom d'équipe officiel** : confirmer si « hla » (Hugo + Lorenzo + Ahmed) est bien le nom déposé → conditionne la validité du group Maven `fr.esgi.hla`
- **Dérogation équipe 3/4** : à assumer en slide 1 de la présentation

## Notes libres / questions en plus

→
