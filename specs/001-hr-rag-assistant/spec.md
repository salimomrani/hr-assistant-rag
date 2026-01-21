# Feature Specification: HR RAG Assistant

**Feature Branch**: `001-hr-rag-assistant`
**Created**: 2026-01-21
**Status**: Draft
**Input**: User description: "Assistant RH intelligent avec RAG pour répondre aux questions des employés basé sur les documents internes"

## Clarifications

### Session 2026-01-21

- Q: Comportement pour les questions hors-sujet (non liées aux RH) ? → A: Refuser poliment et suggérer de contacter les RH
- Q: Taille des segments (chunks) pour le découpage des documents ? → A: 500 caractères avec 50 de chevauchement
- Q: Comportement lors de l'indisponibilité du LLM ? → A: Message d'erreur explicite demandant de réessayer plus tard

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Poser une question RH (Priority: P1)

En tant qu'employé, je veux poser une question sur les politiques RH (congés, avantages, procédures) et recevoir une réponse précise basée sur les documents officiels de l'entreprise.

**Why this priority**: C'est la fonctionnalité principale de l'assistant. Sans cette capacité de répondre aux questions, le système n'a aucune valeur.

**Independent Test**: Peut être testé en posant une question simple comme "Combien de jours de congés ai-je droit ?" et en vérifiant que la réponse est extraite des documents RH indexés.

**Acceptance Scenarios**:

1. **Given** des documents RH sont indexés dans le système, **When** un employé pose une question sur les congés, **Then** le système retourne une réponse pertinente avec les sources citées
2. **Given** des documents RH sont indexés, **When** un employé pose une question dont la réponse n'existe pas dans les documents, **Then** le système indique clairement qu'il ne peut pas répondre et suggère de contacter les RH
3. **Given** des documents RH sont indexés, **When** un employé pose une question ambiguë, **Then** le système demande des clarifications ou fournit plusieurs interprétations possibles

---

### User Story 2 - Indexer des documents RH (Priority: P2)

En tant qu'administrateur RH, je veux uploader et indexer des documents (PDF, TXT) pour que l'assistant puisse répondre aux questions basées sur leur contenu.

**Why this priority**: Sans documents indexés, l'assistant ne peut pas fournir de réponses. Cette fonctionnalité est essentielle mais secondaire car on peut pré-charger des documents au démarrage.

**Independent Test**: Peut être testé en uploadant un document PDF contenant une politique de congés, puis en vérifiant qu'une question sur les congés retourne des informations de ce document.

**Acceptance Scenarios**:

1. **Given** je suis sur l'interface d'administration, **When** j'uploade un fichier PDF valide, **Then** le document est indexé et apparaît dans la liste des documents
2. **Given** un document est indexé, **When** je le supprime, **Then** les informations de ce document ne sont plus utilisées pour les réponses
3. **Given** j'uploade un fichier non supporté (ex: .exe), **When** le système traite la requête, **Then** un message d'erreur clair est affiché

---

### User Story 3 - Recevoir les réponses en streaming (Priority: P3)

En tant qu'employé, je veux voir la réponse s'afficher progressivement (token par token) pour une meilleure expérience utilisateur et savoir que le système travaille.

**Why this priority**: Améliore l'expérience utilisateur mais n'est pas critique pour la fonctionnalité de base. Une réponse complète après traitement fonctionne aussi.

**Independent Test**: Peut être testé en posant une question et en vérifiant que la réponse apparaît progressivement au lieu d'un bloc après délai.

**Acceptance Scenarios**:

1. **Given** je pose une question via l'interface de streaming, **When** le système génère la réponse, **Then** les mots apparaissent progressivement à l'écran
2. **Given** une erreur survient pendant la génération, **When** le streaming est en cours, **Then** l'erreur est communiquée clairement et le streaming s'arrête proprement

---

### User Story 4 - Consulter les documents indexés (Priority: P4)

En tant qu'administrateur RH, je veux voir la liste des documents actuellement indexés pour gérer la base de connaissances de l'assistant.

**Why this priority**: Fonctionnalité de gestion utile mais non critique pour le MVP.

**Independent Test**: Peut être testé en vérifiant que la liste affiche tous les documents uploadés avec leurs métadonnées.

**Acceptance Scenarios**:

1. **Given** plusieurs documents sont indexés, **When** je consulte la liste, **Then** tous les documents sont affichés avec leur nom et date d'indexation
2. **Given** aucun document n'est indexé, **When** je consulte la liste, **Then** un message indique que la base est vide

---

### Edge Cases

- Que se passe-t-il si un document uploadé est corrompu ou illisible ?
- Questions hors-sujet (non liées aux RH) : le système refuse poliment et suggère de contacter les RH
- Service LLM indisponible : retourner un message d'erreur explicite demandant de réessayer plus tard
- Comment gérer les documents en plusieurs langues ?
- Que se passe-t-il si un employé pose une question sur des informations confidentielles auxquelles il n'a pas accès ?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Le système DOIT permettre aux employés de poser des questions en langage naturel
- **FR-002**: Le système DOIT rechercher les informations pertinentes dans les documents indexés avant de générer une réponse
- **FR-003**: Le système DOIT citer les sources (noms des documents) utilisées pour générer chaque réponse
- **FR-004**: Le système DOIT permettre l'upload de documents aux formats PDF et TXT
- **FR-005**: Le système DOIT découper les documents en segments (chunks) pour une recherche efficace
- **FR-006**: Le système DOIT convertir les segments en vecteurs pour la recherche sémantique
- **FR-007**: Le système DOIT supporter le streaming des réponses (Server-Sent Events)
- **FR-008**: Le système DOIT permettre de lister tous les documents indexés
- **FR-009**: Le système DOIT permettre de supprimer un document de l'index
- **FR-010**: Le système DOIT retourner un message approprié quand aucune information pertinente n'est trouvée
- **FR-011**: Le système DOIT fournir un endpoint de santé pour vérifier sa disponibilité
- **FR-012**: Le système DOIT retourner un message d'erreur explicite demandant de réessayer plus tard si le service LLM est indisponible
- **FR-013**: Le système DOIT refuser poliment les questions hors-sujet (non liées aux RH) et suggérer de contacter le service RH

### Key Entities

- **Document**: Représente un fichier RH uploadé (nom, type, date d'upload, statut d'indexation)
- **Segment (Chunk)**: Portion de document de 500 caractères avec 50 caractères de chevauchement, contenant le texte et la référence au document parent
- **Embedding**: Représentation vectorielle d'un segment pour la recherche sémantique
- **Question**: Requête de l'employé avec optionnellement un identifiant de conversation
- **Réponse**: Texte généré par le LLM avec les sources associées

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Les employés obtiennent une réponse à leur question en moins de 10 secondes (hors temps de streaming)
- **SC-002**: 80% des réponses fournies sont jugées pertinentes et utiles par les utilisateurs
- **SC-003**: Le système peut indexer un document PDF de 50 pages en moins de 60 secondes
- **SC-004**: Les réponses citent correctement les sources dans 95% des cas où l'information provient des documents
- **SC-005**: Le système reste disponible et réactif avec jusqu'à 50 utilisateurs simultanés
- **SC-006**: Réduction de 30% des demandes simples adressées directement au service RH
- **SC-007**: Les utilisateurs peuvent commencer à voir la réponse en streaming en moins de 2 secondes après avoir posé leur question

## Assumptions

- Ollama avec le modèle llama3.2 est installé et accessible localement sur le port 11434
- Les documents RH sont en français
- Les utilisateurs ont un accès réseau au serveur backend
- Tous les employés ont le même niveau d'accès aux documents indexés (pas de gestion de permissions granulaire)
- Le stockage vectoriel en mémoire est suffisant pour le MVP (migration vers pgvector prévue ultérieurement)
- Les documents uploadés ne contiennent pas d'informations personnelles sensibles nécessitant un chiffrement spécial

## Out of Scope

- Authentification et gestion des utilisateurs (tous les accès sont ouverts pour le MVP)
- Interface frontend (le backend expose des APIs REST uniquement)
- Historique des conversations persistant
- Multi-tenancy (plusieurs entreprises)
- Support de formats autres que PDF et TXT
- Traduction automatique des documents
- Analyse de sentiment des questions
