# Plan de test Itadaki

Préparation démo jury ESGI — mardi 21 avril 2026 après-midi (15 min).

**Légende** : P0 = bloquant démo · P1 = important · P2 = nice-to-have · ⬜ à tester · 🟡 en cours · ✅ OK · ❌ bug

## 1. BF sujet (obligatoires) · **P0**

| # | Test | Statut | Notes |
|---|---|---|---|
| BF1 | Inscription email + username + password | ✅ | Testé avec testeur@itadaki.fr — retour 201 + JWT |
| BF2 | Connexion credentials valides | ✅ | Retour 200 + JWT |
| BF2-neg | Login avec password invalide | ⬜ | Doit renvoyer 401 + message clair |
| BF3a | Upload photo réelle (image.png) + analyse Ollama | ✅ | Sauté de saumon · 6/6 ingrédients · 383-518 kcal · ~3 min (resize+upload+analyse+polling) |
| BF3b | 2ᵉ passe LLM avec indication texte | ⬜ | Via "Relancer l'analyse" |
| BF4a | Historique liste des repas analysés | ✅ | Dashboard + Historique OK |
| BF4b | Historique calories ingérées par jour | ✅ | Graphique 7 jours OK |
| BF5 | Déconnexion → retour écran auth | ⬜ | Via Profil → Déconnexion |

## 2. API REST isolée · **P0**

| # | Endpoint | Cas | Statut |
|---|---|---|---|
| 2.1 | POST /api/auth/register | Email dupliqué | ⬜ (front gère 404 vs 409) |
| 2.2 | POST /api/auth/register | Username dupliqué | ⬜ |
| 2.3 | POST /api/auth/register | Password < 8 car. | ⬜ (validation front ou back ?) |
| 2.4 | POST /api/auth/login | Password invalide | ⬜ Doit 401 |
| 2.5 | POST /api/meals | Sans auth JWT | ⬜ Doit 401 |
| 2.6 | POST /api/meals | Fichier non-image | ⬜ Via validator ValidImageFile |
| 2.7 | POST /api/meals | Fichier > 10 Mo | ⬜ Multipart limit |
| 2.8 | POST /api/analyses/{id} | body vide | ✅ Déclenche analyse |
| 2.9 | POST /api/analyses/{id} | body {"hint":"..."} | ⬜ 2ᵉ passe |
| 2.10 | GET /api/analyses/{id} | Avant analyse | ⬜ 404 attendu |
| 2.11 | GET /api/analyses/{id} | Après analyse | ✅ 200 + DTO |
| 2.12 | GET /api/history?page=0&size=20 | Page vide | ✅ |
| 2.13 | GET /api/history?page=0&size=20 | Page peuplée | ✅ 2 repas visibles |
| 2.14 | GET /api/history/date/YYYY-MM-DD | Date avec repas | ⬜ |
| 2.15 | GET /api/stats/overview | User sans meal | ✅ |
| 2.16 | GET /api/stats/daily?from=...&to=... | Range 7 jours | ✅ |
| 2.17 | DELETE /api/meals/{id} | Own meal | ⬜ |
| 2.18 | DELETE /api/meals/{id} | Other user meal | ⬜ Doit 403/unauthorized |

## 3. Front UX · **P1**

| # | Test | Statut |
|---|---|---|
| 3.1 | Reload avec token → dashboard direct (bootstrap) | ⬜ |
| 3.2 | Navigation sidebar Analyser / Historique / Profil | ⬜ |
| 3.3 | Drag & drop image vs file picker | ⬜ |
| 3.4 | Cancel upload avant clic Analyser | ⬜ |
| 3.5 | Correction 2ᵉ passe : nouvelle analyse remplace l'ancienne | ⬜ |
| 3.6 | Détail repas depuis l'historique | ⬜ |
| 3.7 | Logout → localStorage nettoyé | ⬜ |
| 3.8 | Responsive mobile (< 640px) | ⬜ |
| 3.9 | Mode sombre (si dispo) | ⬜ |

## 4. Edge cases & erreurs · **P1**

| # | Test | Statut |
|---|---|---|
| 4.1 | Token expiré → redirect auth automatique | ⬜ |
| 4.2 | Token tampered → 401 + redirect | ⬜ |
| 4.3 | Ollama down (ngrok tunnel coupé) → message clair | ⬜ |
| 4.4 | Image corrompue (octets aléatoires) | ⬜ |
| 4.5 | Password < 8 caractères (validation) | ⬜ |
| 4.6 | Email invalide format | ⬜ |
| 4.7 | Username avec caractères spéciaux | ⬜ |
| 4.8 | Network offline pendant analyse | ⬜ |
| 4.9 | Reload browser pendant analyse → polling reprend ? | ⬜ |
| 4.10 | XSS tentative dans hint ou username | ⬜ |

## 5. Performance & charge · **P2**

| # | Test | Statut |
|---|---|---|
| 5.1 | Cold start Ollama après redémarrage | ⬜ ~50 s attendu |
| 5.2 | 5 analyses enchaînées rapidement | ⬜ Rate limit ngrok free 40/min |
| 5.3 | Image proche 10 Mo | ⬜ |
| 5.4 | 100 repas en historique — pagination | ⬜ |

## 6. Démo jury · **P0**

| # | Item | Statut |
|---|---|---|
| 6.1 | Script démo 12 min + 3 min Q&A | ⬜ |
| 6.2 | Vidéo 3 min backup pré-enregistrée | ⬜ |
| 6.3 | DataSeeder : 3 comptes démo avec historique rempli | ⬜ |
| 6.4 | Slides 15 min + 2 répétitions chronométrées | ⬜ |
| 6.5 | Machine Ollama : warm-up Hugo confirmé avant démo | ⬜ |
| 6.6 | ngrok tunnel Hugo actif (URL `ducky-shrank-washer.ngrok-free.dev`) | ⬜ |
| 6.7 | Plan B : `OLLAMA_URL=http://localhost:11434` si Hugo down | ⬜ |

## Bugs connus (tracking)

- ❌ favicon.ico 500 (cosmétique, pas bloquant)
- ⚠ `MealAnalysisResponseDto.estimatedTotalCalories` persiste seulement `caloriesMax` (front compense en appliquant ±15% pour la fourchette)
- ⚠ Register retourne 404 (au lieu de 409) si email/username déjà pris — front gère
- ⚠ `reanalyzeMeal` **delete** l'ancienne analyse (pas d'historique des passes) — interprétation BF4 "la ou des analyses" à défendre en Q&A

## Sources

- Sujet PDF : [`Sujet.pdf`](./Sujet.pdf)
- Spec IA : [`AI_Model/VALIDATION-OLLAMA.md`](./AI_Model/VALIDATION-OLLAMA.md)
- Conventions code : [`CONVENTIONS.md`](./CONVENTIONS.md)
- Wiki projet : `../wiki/projets/itadaki.md`
