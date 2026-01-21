# Data Model: HR RAG Assistant

**Date**: 2026-01-21
**Feature**: 001-hr-rag-assistant

## Entities

### Document

Représente un fichier RH uploadé et indexé.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK, auto-generated | Identifiant unique |
| name | String | Required, max 255 | Nom original du fichier |
| type | Enum | PDF, TXT | Type de document |
| contentHash | String | SHA-256 | Hash pour déduplication |
| uploadedAt | Instant | Auto-set | Date/heure d'upload |
| indexedAt | Instant | Nullable | Date/heure d'indexation |
| status | Enum | PENDING, INDEXED, FAILED | Statut d'indexation |
| chunkCount | Integer | >= 0 | Nombre de chunks générés |
| errorMessage | String | Nullable | Message si échec |

**Validation Rules**:
- `name` ne doit pas être vide
- `type` doit être PDF ou TXT
- `status` initial = PENDING

---

### DocumentChunk

Segment de document avec son embedding associé.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK, auto-generated | Identifiant unique |
| documentId | UUID | FK → Document.id | Référence au document parent |
| content | String | Required, ~500 chars | Texte du segment |
| chunkIndex | Integer | >= 0 | Position dans le document |
| startOffset | Integer | >= 0 | Offset de début dans le texte original |
| endOffset | Integer | > startOffset | Offset de fin |
| embedding | float[] | Vector, nullable | Représentation vectorielle |

**Validation Rules**:
- `content` entre 1 et 600 caractères (tolérance overlap)
- `chunkIndex` unique par `documentId`
- `embedding` généré après création

---

### ChatRequest (DTO - Input)

Requête de l'employé.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| question | String | Required, 1-2000 chars | Question en langage naturel |
| conversationId | UUID | Optional | ID pour grouper les échanges |

**Validation Rules**:
- `question` ne doit pas être vide ou que des espaces
- `question` max 2000 caractères

---

### ChatResponse (DTO - Output)

Réponse générée par le système.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| answer | String | Required | Réponse générée par le LLM |
| sources | List<String> | May be empty | Noms des documents sources |
| conversationId | UUID | Always present | ID de conversation |
| error | ErrorInfo | Nullable | Info erreur si applicable |

---

### DocumentInfo (DTO)

Métadonnées d'un document pour l'API.

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Identifiant du document |
| name | String | Nom du fichier |
| type | String | "PDF" ou "TXT" |
| uploadedAt | String (ISO-8601) | Date d'upload |
| status | String | Statut d'indexation |
| chunkCount | Integer | Nombre de chunks |

---

### ErrorInfo (DTO)

Informations d'erreur standardisées.

| Field | Type | Description |
|-------|------|-------------|
| code | String | Code erreur (HR-001, etc.) |
| message | String | Message utilisateur |
| details | String | Détails techniques (optionnel) |

---

## State Transitions

### Document Status

```
                ┌─────────┐
                │ PENDING │
                └────┬────┘
                     │
          ┌──────────┴──────────┐
          │                     │
          ▼                     ▼
    ┌─────────┐           ┌─────────┐
    │ INDEXED │           │ FAILED  │
    └─────────┘           └─────────┘
```

- **PENDING → INDEXED**: Chunking et embedding réussis
- **PENDING → FAILED**: Erreur de parsing ou embedding

---

## Relationships

```
┌──────────────┐         ┌─────────────────┐
│   Document   │ 1 ───── * │ DocumentChunk  │
│              │         │                 │
│ id           │         │ id              │
│ name         │         │ documentId (FK) │
│ type         │         │ content         │
│ status       │         │ chunkIndex      │
│ chunkCount   │         │ embedding       │
└──────────────┘         └─────────────────┘
```

---

## Storage Notes

### MVP (In-Memory)
- `Document` stocké dans `ConcurrentHashMap<UUID, Document>`
- `DocumentChunk` intégré dans `InMemoryEmbeddingStore` de LangChain4j
- Données perdues au redémarrage (acceptable pour MVP)

### Future (pgvector)
- Tables PostgreSQL avec extension pgvector
- Index HNSW pour recherche vectorielle efficace
- Persistance complète
