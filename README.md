# HR Assistant RAG

Assistant RH intelligent basé sur RAG (Retrieval-Augmented Generation) pour répondre aux questions des employés.

## Structure du projet (Monorepo)

```
hr-assistant-rag/
├── backend/          # API Spring Boot
├── frontend/         # Application Angular (PrimeNG)
├── specs/            # Spécifications et documentation
├── CLAUDE.md         # Guide pour Claude Code
└── README.md         # Ce fichier
```

## Démarrage rapide

### Prérequis

- Java 21+
- Maven 3.8+
- Node.js 22+
- Docker (pour PostgreSQL + Redis)
- Ollama installé et en cours d'exécution

### 1. Installation d'Ollama

```bash
# macOS
brew install ollama

# Linux
curl -fsSL https://ollama.com/install.sh | sh

# Démarrer Ollama
ollama serve

# Télécharger les modèles
ollama pull llama3.2
ollama pull nomic-embed-text
```

### 2. Backend (API)

```bash
cd backend
mvn spring-boot:run
```

Docker Compose démarre automatiquement PostgreSQL (pgvector) et Redis via Spring Boot Docker Compose Support.

L'API sera disponible sur `http://localhost:8080`

Voir [backend/README.md](backend/README.md) pour plus de détails.

### 3. Frontend

```bash
cd frontend
npm install
npm start
```

L'application sera disponible sur `http://localhost:4200` (proxy configuré vers le backend).

## Technologies

### Backend
- **Java 21** + **Spring Boot 4.0.1**
- **Spring AI 2.0.0-M1** (intégration LLM)
- **Ollama** (llama3.2 + nomic-embed-text)
- **PostgreSQL 16 + pgvector** (vector store)
- **Redis 7.4** (semantic caching)
- **Spring WebFlux** (streaming SSE)
- **PDFBox 3.0.6** (parsing PDF)
- **MapStruct 1.6.3** + **Lombok**

### Frontend
- **Angular 21** + **TypeScript 5.9**
- **PrimeNG 21** (composants UI)
- **RxJS 7.8** (streams réactifs)
- **ngx-markdown** (rendu Markdown)

### Infrastructure
- **Docker Compose** (PostgreSQL pgvector + Redis)
- **Spring Boot Docker Compose Support** (démarrage automatique)

## Architecture

Le système utilise une architecture RAG (Retrieval-Augmented Generation) :

1. **Indexation** : Upload de documents PDF/TXT → Extraction de texte → Chunking → Embeddings → pgvector
2. **Récupération** : Question → Embedding → Recherche de similarité (pgvector HNSW) → Top-K chunks
3. **Génération** : Chunks + Question → LLM (llama3.2) → Réponse avec sources
4. **Caching** : Réponses mises en cache dans Redis (cache sémantique)

## API Endpoints

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/health` | GET | Health check |
| `/api/chat` | POST | Question/réponse (blocking) |
| `/api/chat/stream` | POST | Question/réponse (streaming SSE) |
| `/api/documents` | POST | Upload document |
| `/api/documents` | GET | Liste documents |
| `/api/documents/{id}` | DELETE | Supprimer document |

## Documentation

- **Spécifications**: [specs/001-hr-rag-assistant/](specs/001-hr-rag-assistant/)
- **Guide de démarrage**: [specs/001-hr-rag-assistant/quickstart.md](specs/001-hr-rag-assistant/quickstart.md)
- **Tâches**: [specs/001-hr-rag-assistant/tasks.md](specs/001-hr-rag-assistant/tasks.md)

## Contribuer

Ce projet utilise Claude Code pour le développement assisté par IA.

Voir [CLAUDE.md](CLAUDE.md) pour les conventions de développement.

## License

Propriétaire
