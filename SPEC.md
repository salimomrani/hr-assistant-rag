# HR Assistant RAG - Spécifications

## Objectif
Créer un assistant RH intelligent qui répond aux questions des employés en se basant sur les documents internes (RAG).

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND (futur)                      │
│                     Angular + RxJS                           │
└────────────────────────────┬────────────────────────────────┘
                             │ HTTP / SSE
┌────────────────────────────┴────────────────────────────────┐
│                        BACKEND                               │
│                  Spring Boot + LangChain4j                   │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ ChatController│  │ RagService   │  │ EmbeddingService │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │GuardrailSvc  │  │ CacheService │  │ DocumentService  │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
│                                                              │
│                    ┌──────────────┐                          │
│                    │ VectorStore  │                          │
│                    │ (In-Memory)  │                          │
│                    └──────────────┘                          │
└────────────────────────────┬────────────────────────────────┘
                             │
                      ┌──────┴──────┐
                      │   Ollama    │
                      │ (llama3.2)  │
                      └─────────────┘
```

---

## Stack Technique

| Composant | Technologie |
|-----------|-------------|
| Backend | Spring Boot 4.0.1 |
| IA | LangChain4j 1.10.0 |
| LLM | Ollama (llama3.2) |
| Vector Store | In-Memory (puis pgvector) |
| Streaming | WebFlux / SSE |

---

## Endpoints API

### Chat
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/chat` | Envoyer une question (réponse complète) |
| GET | `/api/chat/stream` | Envoyer une question (streaming SSE) |

### Documents
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/documents` | Upload un document (PDF/TXT) |
| GET | `/api/documents` | Liste des documents indexés |
| DELETE | `/api/documents/{id}` | Supprimer un document |

### Health
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/health` | Vérifier que l'API est up |

---

## Fonctionnalités

### Phase 1 : Core RAG
- [ ] Configuration Ollama (OllamaConfig)
- [ ] Service Embedding (EmbeddingService)
- [ ] Service Vector Store (VectorStoreService)
- [ ] Service RAG (RagService)
- [ ] Controller Chat basique (ChatController)
- [ ] Endpoint `/api/chat` (POST)

### Phase 2 : Documents
- [ ] Service Document (DocumentService)
- [ ] Chunking des documents
- [ ] Indexation dans le vector store
- [ ] Controller Documents (DocumentController)
- [ ] Endpoints CRUD documents

### Phase 3 : Streaming
- [ ] Configuration WebFlux
- [ ] Endpoint `/api/chat/stream` (SSE)
- [ ] Streaming token par token

### Phase 4 : Industrialisation
- [ ] Guardrails (filtrage entrée/sortie)
- [ ] Semantic Caching
- [ ] Gestion des erreurs
- [ ] Logging structuré

### Phase 5 : Frontend Angular (optionnel)
- [ ] Interface chat
- [ ] Upload documents
- [ ] Affichage streaming

---

## Structure du Projet

```
src/main/java/com/hrassistant/
├── HrAssistantApplication.java
├── config/
│   └── OllamaConfig.java
├── controller/
│   ├── ChatController.java
│   └── DocumentController.java
├── service/
│   ├── EmbeddingService.java
│   ├── VectorStoreService.java
│   ├── RagService.java
│   ├── DocumentService.java
│   ├── GuardrailService.java
│   └── CacheService.java
├── model/
│   ├── ChatRequest.java
│   ├── ChatResponse.java
│   └── DocumentInfo.java
└── exception/
    └── HrAssistantException.java
```

---

## Modèles (DTOs)

### ChatRequest
```java
{
  "question": "Combien de jours de congés ai-je ?",
  "conversationId": "uuid" // optionnel, pour l'historique
}
```

### ChatResponse
```java
{
  "answer": "Vous avez droit à 25 jours...",
  "sources": ["guide_conges.pdf"],
  "conversationId": "uuid"
}
```

---

## Étapes de Développement

| Étape | Tâche | Statut |
|-------|-------|--------|
| 1 | Créer OllamaConfig | ⬜ À faire |
| 2 | Créer EmbeddingService | ⬜ À faire |
| 3 | Créer VectorStoreService | ⬜ À faire |
| 4 | Créer RagService | ⬜ À faire |
| 5 | Créer ChatController | ⬜ À faire |
| 6 | Tester endpoint /api/chat | ⬜ À faire |
| 7 | Ajouter DocumentService | ⬜ À faire |
| 8 | Ajouter streaming | ⬜ À faire |
| 9 | Ajouter guardrails | ⬜ À faire |
| 10 | Ajouter caching | ⬜ À faire |

---

## Notes

- On commence simple (in-memory) puis on complexifie
- Chaque étape doit être testable indépendamment
- L'utilisateur code, Claude assiste et explique
