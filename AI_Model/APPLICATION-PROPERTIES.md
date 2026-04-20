# Config à ajouter dans `application.properties`

**Ahmed a déjà posé la config H2** (commit `ecb7b04`). Ajouter les sections suivantes.

## Config actuelle (Ahmed)

```properties
spring.application.name=itadaki

spring.datasource.url=jdbc:h2:file:./data/itadaki
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## Ajouts nécessaires

```properties
# --- Spring AI Ollama ---
spring.ai.ollama.base-url=${OLLAMA_HOST:http://localhost:11434}
spring.ai.ollama.chat.options.model=qwen2.5vl:7b
spring.ai.ollama.chat.options.temperature=0.2
spring.ai.ollama.chat.options.num-ctx=4096
spring.ai.ollama.chat.options.keep-alive=10m
# ⚠ vérifier 2.0-M4 : en 1.x, 'init.*' existe ; en 2.0-M4 le namespace peut différer
spring.ai.ollama.init.pull-model-strategy=when_missing
spring.ai.ollama.init.timeout=10m

# HTTP client timeouts (appel local lent en CPU)
spring.ai.ollama.client.read-timeout=180s
spring.ai.ollama.client.connect-timeout=10s

# --- Multipart upload photo (BF3) ---
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=12MB
app.uploads.dir=./uploads

# --- Jackson / timezone (agrégation calories/jour Europe/Paris) ---
spring.jackson.time-zone=Europe/Paris
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss.SSSXXX

# --- springdoc (Swagger UI) ---
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha

# --- Security (seeds pré-configurés pour démo, optionnel si DataSeeder Java) ---
# spring.security.user.name et password sont ignorés si UserDetailsService custom :
# les comptes seront seedés par fr.esgi.hla.itadaki.config.DataSeeder (CommandLineRunner)

# --- Logging Spring AI (debug pendant dev, à retirer pour la démo) ---
logging.level.org.springframework.ai=INFO
logging.level.fr.esgi.hla.itadaki=DEBUG
```

## Fichier `application.properties` complet attendu

Une fois les ajouts faits, le fichier devrait ressembler à ceci :

```properties
spring.application.name=itadaki

# --- Database ---
spring.datasource.url=jdbc:h2:file:./data/itadaki
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# --- Spring AI Ollama ---
spring.ai.ollama.base-url=${OLLAMA_HOST:http://localhost:11434}
spring.ai.ollama.chat.options.model=qwen2.5vl:7b
spring.ai.ollama.chat.options.temperature=0.2
spring.ai.ollama.chat.options.num-ctx=4096
spring.ai.ollama.chat.options.keep-alive=10m
spring.ai.ollama.init.pull-model-strategy=when_missing
spring.ai.ollama.init.timeout=10m
spring.ai.ollama.client.read-timeout=180s
spring.ai.ollama.client.connect-timeout=10s

# --- Upload ---
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=12MB
app.uploads.dir=./uploads

# --- Timezone ---
spring.jackson.time-zone=Europe/Paris

# --- Swagger UI ---
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha

# --- Logging ---
logging.level.fr.esgi.hla.itadaki=DEBUG
```

## Plan B si Spring AI 2.0-M4 coince

Si Ahmed tente `mvn spring-boot:run` et que ça crash à cause de clés inconnues `spring.ai.ollama.*` (2.0-M4 les a renommées), 2 options :

**Option 1 — Rester en 2.0-M4 avec config minimale**

Laisser seulement les 2 clés essentielles :
```properties
spring.ai.ollama.base-url=${OLLAMA_HOST:http://localhost:11434}
spring.ai.ollama.chat.options.model=qwen2.5vl:7b
```
Et enlever `init.*`, `client.*`, `keep-alive`. Le reste passe en valeurs par défaut acceptables.

**Option 2 — Rollback Spring AI 1.1.4 GA**

Dans `pom.xml` :
```xml
<properties>
    <java.version>21</java.version>  <!-- 1.1.4 exige Java 21 LTS -->
    <spring-ai.version>1.1.4</spring-ai.version>
</properties>
```

Et changer le `<parent>` Spring Boot de `4.0.5` → `3.5.x` (la dernière 3.5 stable).

Décision : laisser Ahmed juger en fonction de ce qu'il voit au `mvn compile` + hello-world Ollama.
