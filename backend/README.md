# HR Assistant RAG - Backend

API Spring Boot pour l'assistant RH intelligent basé sur RAG.

## Démarrage

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Run sur un port différent
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

L'API sera disponible sur `http://localhost:8080`

## Prérequis

- Java 17+
- Maven 3.8+
- Ollama en cours d'exécution sur port 11434

```bash
ollama serve
ollama pull llama3.2
ollama pull nomic-embed-text
```

## Structure du projet

```
backend/
├── src/
│   └── main/
│       ├── java/com/hrassistant/
│       │   ├── config/           # Configuration Spring
│       │   ├── controller/       # REST endpoints
│       │   ├── service/          # Business logic
│       │   ├── model/            # DTOs
│       │   ├── mapper/           # MapStruct mappers
│       │   └── exception/        # Exception handling
│       └── resources/
│           ├── application.yml   # Configuration
│           └── prompts/          # LLM prompts
├── target/                       # Build artifacts
└── pom.xml                       # Maven dependencies
```

## Configuration

Fichier `src/main/resources/application.yml`:

```yaml
hr-assistant:
  ollama:
    base-url: http://localhost:11434
    model: llama3.2
    embedding-model: nomic-embed-text
    timeout: 60s
  rag:
    chunk-size: 500
    chunk-overlap: 50
    max-results: 5
    similarity-threshold: 0.3
  documents:
    max-size-mb: 10
```

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

- **Spring Boot**: 4.0.1
- **Java**: 17
- **LangChain4j**: 1.10.0 (LLM orchestration)
- **Ollama**: Local LLM (llama3.2 + nomic-embed-text)
- **PDFBox**: 3.0.6 (PDF parsing)
- **MapStruct**: 1.6.3 (object mapping)
- **Lombok**: Boilerplate reduction
- **Jakarta Validation**: Input validation
- **WebFlux**: Reactive streaming (SSE)

## Architecture RAG

### Pipeline d'indexation

1. **Upload** : Validation (type, taille)
2. **Extraction** : PDFBox pour PDF, UTF-8 pour TXT
3. **Chunking** : Découpage en chunks de 500 caractères avec overlap de 50
4. **Embedding** : Génération de vecteurs avec nomic-embed-text
5. **Stockage** : InMemoryEmbeddingStore (VectorDB)

### Pipeline de réponse

1. **Validation** : GuardrailService (off-topic detection)
2. **Embedding** : Transformation de la question en vecteur
3. **Recherche** : Top-5 chunks similaires (seuil: 0.3)
4. **Contexte** : Assemblage des chunks pertinents
5. **Génération** : LLM (llama3.2) avec prompt + contexte
6. **Sources** : Extraction des documents sources cités

## Services

- **RagService**: Orchestration RAG (blocking)
- **StreamingRagService**: Orchestration RAG (streaming SSE)
- **DocumentService**: Upload, chunking, indexation
- **EmbeddingService**: Génération d'embeddings
- **VectorStoreService**: Recherche vectorielle
- **GuardrailService**: Validation des questions

## Logging

Tous les services utilisent SLF4J avec niveaux appropriés:
- **INFO**: Entry/exit points, résultats
- **DEBUG**: Détails techniques
- **WARN**: Situations anormales
- **ERROR**: Erreurs avec stack traces

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

1. Vérifier qu'Ollama est lancé: `ollama list`
2. Vérifier le port: `curl http://localhost:11434`
3. Vérifier les logs Spring Boot

### Erreur "Document invalide"

- Type supporté: PDF, TXT uniquement
- Taille max: 10MB
- PDFs non protégés par mot de passe

### Port 8080 déjà utilisé

```bash
# Tuer le processus
lsof -ti:8080 | xargs kill -9

# Ou utiliser un autre port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

## Documentation complète

Voir `../specs/001-hr-rag-assistant/` pour la documentation complète.
