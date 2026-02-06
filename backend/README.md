# HR Assistant RAG - Backend

API Spring Boot pour l'assistant RH intelligent basé sur RAG.

## Démarrage

### Prérequis

- Java 21+
- Maven 3.8+
- Docker (pour PostgreSQL pgvector + Redis)
- Ollama en cours d'exécution sur port 11434

```bash
ollama serve
ollama pull llama3.2
ollama pull nomic-embed-text
```

### Lancer l'application

```bash
# Build
mvn clean install

# Run (Docker Compose démarre automatiquement PostgreSQL + Redis)
mvn spring-boot:run

# Run sur un port différent
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

L'API sera disponible sur `http://localhost:8080`

## Structure du projet

```
backend/
├── src/
│   └── main/
│       ├── java/com/hrassistant/
│       │   ├── config/           # Configuration Spring (Redis, Web, Logging)
│       │   ├── controller/       # REST endpoints (Chat, Document, Health)
│       │   ├── service/          # Business logic (RAG, Embedding, Cache)
│       │   ├── model/            # DTOs et entités JPA
│       │   ├── mapper/           # MapStruct mappers
│       │   ├── repository/       # Spring Data JPA repositories
│       │   └── exception/        # Exception handling
│       └── resources/
│           ├── application.yml   # Configuration
│           └── prompts/          # LLM prompts
├── docker-compose.yml            # PostgreSQL pgvector + Redis
├── target/                       # Build artifacts
└── pom.xml                       # Maven dependencies
```

## Configuration

Fichier `src/main/resources/application.yml`:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.2
        options:
          temperature: 0.7
      embedding:
        model: nomic-embed-text
    vectorstore:
      pgvector:
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 768

hr-assistant:
  rag:
    chunk-size: 500
    chunk-overlap: 50
    max-results: 5
    similarity-threshold: 0.3
  cache:
    enabled: true
    ttl-seconds: 3600
    similarity-threshold: 0.85
```

## Docker Compose

Le `docker-compose.yml` fournit :
- **PostgreSQL 16 + pgvector** : vector store pour les embeddings
- **Redis 7.4** : cache sémantique des réponses

Spring Boot Docker Compose Support démarre automatiquement les containers au lancement de l'application.

## API Endpoints

### Health Check
```bash
curl http://localhost:8080/api/health
```

### Upload Document
```bash
curl -X POST http://localhost:8080/api/documents \
  -F "file=@document.pdf"
```

### List Documents
```bash
curl http://localhost:8080/api/documents
```

### Ask Question (Blocking)
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Combien de jours de congés?"}'
```

### Ask Question (Streaming)
```bash
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -N \
  -d '{"question": "Combien de jours de congés?"}'
```

### Delete Document
```bash
curl -X DELETE http://localhost:8080/api/documents/{id}
```

## Technologies

- **Spring Boot** 4.0.1
- **Java** 21
- **Spring AI** 2.0.0-M1 (intégration LLM + vector store)
- **Ollama** (llama3.2 + nomic-embed-text)
- **PostgreSQL 16 + pgvector** (vector store)
- **Redis 7.4** (semantic caching)
- **PDFBox** 3.0.6 (parsing PDF)
- **MapStruct** 1.6.3 (object mapping)
- **Lombok** (boilerplate reduction)
- **Jakarta Validation** (input validation)
- **Spring WebFlux** (streaming SSE)
- **Spring Data JPA** (persistence)

## Architecture RAG

### Pipeline d'indexation

1. **Upload** : Validation (type, taille)
2. **Extraction** : PDFBox pour PDF, UTF-8 pour TXT
3. **Chunking** : Découpage en chunks de 500 caractères avec overlap de 50
4. **Embedding** : Génération de vecteurs avec nomic-embed-text (Spring AI)
5. **Stockage** : pgvector (PostgreSQL) avec index HNSW

### Pipeline de réponse

1. **Cache** : Vérification du cache sémantique (Redis)
2. **Validation** : GuardrailService (off-topic detection)
3. **Embedding** : Transformation de la question en vecteur
4. **Recherche** : Top-5 chunks similaires via pgvector (seuil: 0.3)
5. **Contexte** : Assemblage des chunks pertinents
6. **Génération** : LLM (llama3.2) avec prompt + contexte
7. **Sources** : Extraction des documents sources cités
8. **Mise en cache** : Stockage de la réponse dans Redis

## Services

- **RagService** : Orchestration RAG (blocking)
- **StreamingRagService** : Orchestration RAG (streaming SSE)
- **CachingStreamingRagService** : Orchestration RAG avec cache sémantique
- **DocumentService** : Upload, chunking, indexation
- **EmbeddingService** : Génération d'embeddings via Spring AI
- **VectorStoreService** : Recherche vectorielle (pgvector)
- **CacheService** : Cache sémantique (Redis)
- **GuardrailService** : Validation des questions

## Logging

Tous les services utilisent SLF4J avec niveaux appropriés:
- **INFO** : Entry/exit points, résultats
- **DEBUG** : Détails techniques
- **WARN** : Situations anormales
- **ERROR** : Erreurs avec stack traces

Request/Response logging automatique via `LoggingInterceptor`.

## Tests

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Build

```bash
# Clean build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Create executable JAR
mvn clean package
java -jar target/hr-assistant-rag-1.0.0.jar
```

## Troubleshooting

### Erreur "Service LLM indisponible"

1. Vérifier qu'Ollama est lancé : `ollama list`
2. Vérifier le port : `curl http://localhost:11434`
3. Vérifier les logs Spring Boot

### Erreur "Document invalide"

- Type supporté : PDF, TXT uniquement
- Taille max : 10MB
- PDFs non protégés par mot de passe

### Port 8080 déjà utilisé

```bash
# Tuer le processus
lsof -ti:8080 | xargs kill -9

# Ou utiliser un autre port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### Docker Compose ne démarre pas

```bash
# Vérifier que Docker est lancé
docker info

# Démarrer manuellement
cd backend
docker compose up -d
```

## Documentation complète

Voir `../specs/001-hr-rag-assistant/` pour la documentation complète.
