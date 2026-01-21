# Research: HR RAG Assistant

**Date**: 2026-01-21
**Feature**: 001-hr-rag-assistant

## LangChain4j RAG Pattern

### Decision
Utiliser l'architecture RAG standard de LangChain4j avec `EmbeddingModel`, `EmbeddingStore`, et `ChatLanguageModel`.

### Rationale
- LangChain4j 1.10.0 fournit une abstraction mature pour RAG
- Support natif d'Ollama via `langchain4j-ollama`
- `InMemoryEmbeddingStore` parfait pour MVP, migration facile vers pgvector
- Pattern bien documenté et testé

### Alternatives Considered
- **Spring AI**: Moins mature, moins de flexibilité pour le chunking
- **Direct Ollama API**: Plus de code boilerplate, pas d'abstraction pour embeddings

---

## Embedding Model Selection

### Decision
Utiliser le modèle d'embedding d'Ollama (`nomic-embed-text` ou modèle par défaut de llama3.2).

### Rationale
- Cohérence: tout passe par Ollama, pas de dépendance externe
- Performance: embeddings locaux, pas de latence réseau
- Coût: gratuit, pas d'API tierce

### Alternatives Considered
- **OpenAI embeddings**: Meilleure qualité mais coût et dépendance externe
- **Sentence Transformers via ONNX**: Complexité d'intégration Java

---

## Chunking Strategy

### Decision
Chunks de 500 caractères avec 50 caractères de chevauchement (overlap).

### Rationale
- Équilibre entre contexte et précision de recherche
- 500 chars ≈ 100-125 mots français, taille idéale pour documents RH
- Overlap de 10% évite la perte de contexte aux frontières

### Alternatives Considered
- **Semantic chunking**: Plus complexe, overkill pour documents RH structurés
- **Fixed token count**: Dépendant du tokenizer, moins prévisible

---

## PDF Parsing

### Decision
Utiliser Apache PDFBox pour l'extraction de texte PDF.

### Rationale
- Bibliothèque Java mature et maintenue
- Bonne extraction de texte pour documents textuels
- Pas de dépendance native

### Alternatives Considered
- **iText**: Licence commerciale restrictive
- **Tika**: Overkill pour PDF uniquement

### Dependencies to Add
```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>
```

---

## Streaming Implementation

### Decision
Utiliser Spring WebFlux avec `Flux<String>` et Server-Sent Events (SSE).

### Rationale
- Spring WebFlux déjà présent dans les dépendances
- SSE standard HTTP, compatible avec tous les clients
- LangChain4j supporte `StreamingChatLanguageModel`

### Alternatives Considered
- **WebSocket**: Bidirectionnel non nécessaire, plus complexe
- **Long polling**: Moins efficace, plus de requêtes

---

## Guardrail Implementation

### Decision
Implémentation simple basée sur des mots-clés et scoring de pertinence.

### Rationale
- MVP: pas besoin de ML complexe
- Détection hors-sujet via score de similarité < seuil
- Liste de mots-clés interdits configurable

### Implementation Approach
1. Calculer similarité entre question et documents
2. Si score max < 0.3 → question hors-sujet
3. Vérifier absence de mots-clés sensibles
4. Retourner message approprié

---

## Error Handling Strategy

### Decision
Exceptions custom avec codes d'erreur standardisés, réponses JSON cohérentes.

### Rationale
- Facilite le debug et monitoring
- UX cohérente pour les clients
- Logging structuré

### Error Codes
| Code | Description |
|------|-------------|
| HR-001 | Service LLM indisponible |
| HR-002 | Document invalide ou corrompu |
| HR-003 | Format de fichier non supporté |
| HR-004 | Question hors-sujet |
| HR-005 | Aucune information pertinente trouvée |

---

## Configuration Properties

### Decision
Configuration externalisée via `application.yml` avec profils Spring.

### Key Properties
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
    allowed-types: pdf,txt
    max-size: 10MB
```

---

## Testing Strategy

### Decision
Tests à trois niveaux: unitaires (mocks), intégration (services), contract (API).

### Rationale
- Tests unitaires rapides pour logique métier
- Tests d'intégration avec Testcontainers si besoin
- Tests contract pour valider les endpoints

### Mocking Approach
- Mock `ChatLanguageModel` et `EmbeddingModel` pour tests unitaires
- Ollama réel pour tests d'intégration (si disponible, sinon skip)
