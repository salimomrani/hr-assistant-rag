# Quickstart: HR RAG Assistant

## Prérequis

1. **Java 17** ou supérieur
2. **Maven 3.8+**
3. **Ollama** installé et en cours d'exécution

## Installation d'Ollama

```bash
# macOS
brew install ollama

# Linux
curl -fsSL https://ollama.com/install.sh | sh

# Démarrer Ollama
ollama serve
```

## Télécharger les modèles requis

```bash
# Modèle de chat
ollama pull llama3.2

# Modèle d'embeddings
ollama pull nomic-embed-text
```

## Lancer l'application

```bash
# Cloner et build
git clone <repo-url>
cd hr-assistant-rag

# Build
mvn clean install

# Lancer
mvn spring-boot:run
```

L'API sera disponible sur `http://localhost:8080`.

## Vérifier que tout fonctionne

```bash
# Health check
curl http://localhost:8080/api/health
```

Réponse attendue:
```json
{
  "status": "UP"
}
```

## Utilisation de base

### 1. Uploader un document

```bash
curl -X POST http://localhost:8080/api/documents \
  -F "file=@mon_document.pdf"
```

### 2. Vérifier l'indexation

```bash
curl http://localhost:8080/api/documents
```

### 3. Poser une question

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Combien de jours de congés ai-je droit ?"}'
```

### 4. Poser une question en streaming

```bash
curl -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -N \
  -d '{"question": "Combien de jours de congés ai-je droit ?"}'
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
    allowed-types: pdf,txt
    max-size: 10MB
```

## Dépannage

### Erreur "Service LLM indisponible"

1. Vérifier qu'Ollama est lancé: `ollama list`
2. Vérifier le port: `curl http://localhost:11434`
3. Vérifier les logs: `mvn spring-boot:run` et regarder les erreurs

### Erreur "Document invalide"

- Vérifier que le fichier est bien un PDF ou TXT
- Vérifier que la taille < 10MB
- Pour PDF: vérifier qu'il n'est pas protégé par mot de passe

## Prochaines étapes

Voir le fichier `tasks.md` pour la liste complète des tâches d'implémentation.
