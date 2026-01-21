# HR Assistant RAG

Assistant RH intelligent basé sur RAG (Retrieval-Augmented Generation) pour répondre aux questions des employés.

## Structure du projet (Monorepo)

```
hr-assistant-rag/
├── backend/          # API Spring Boot
├── frontend/         # Application frontend (à venir)
├── specs/            # Spécifications et documentation
├── CLAUDE.md         # Guide pour Claude Code
└── README.md         # Ce fichier
```

## Démarrage rapide

### Backend (API)

```bash
cd backend
mvn spring-boot:run
```

L'API sera disponible sur `http://localhost:8080`

Voir [backend/README.md](backend/README.md) pour plus de détails.

### Frontend

À venir

## Documentation

- **Spécifications**: [specs/001-hr-rag-assistant/](specs/001-hr-rag-assistant/)
- **Guide de démarrage**: [specs/001-hr-rag-assistant/quickstart.md](specs/001-hr-rag-assistant/quickstart.md)
- **Tâches**: [specs/001-hr-rag-assistant/tasks.md](specs/001-hr-rag-assistant/tasks.md)

## Technologies

### Backend
- Spring Boot 4.0.1
- Java 17
- LangChain4j 1.10.0
- Ollama (llama3.2 + nomic-embed-text)

### Frontend
- À définir

## Prérequis

- Java 17+
- Maven 3.8+
- Ollama installé et en cours d'exécution

## Installation d'Ollama

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

## Architecture

Le système utilise une architecture RAG (Retrieval-Augmented Generation) :

1. **Indexation** : Upload de documents PDF/TXT → Extraction de texte → Chunking → Embeddings → VectorStore
2. **Récupération** : Question → Embedding → Recherche de similarité → Top-K chunks
3. **Génération** : Chunks + Question → LLM → Réponse avec sources

## API Endpoints

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/health` | GET | Health check |
| `/api/chat` | POST | Question/réponse (blocking) |
| `/api/chat/stream` | POST | Question/réponse (streaming SSE) |
| `/api/documents` | POST | Upload document |
| `/api/documents` | GET | Liste documents |
| `/api/documents/{id}` | DELETE | Supprimer document |

## Contribuer

Ce projet utilise Claude Code pour le développement assisté par IA.

Voir [CLAUDE.md](CLAUDE.md) pour les conventions de développement.

## License

Propriétaire
