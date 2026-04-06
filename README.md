# Service Booking & Consulting Platform

**EECS 3311 — Team 1 Project (Phase 2)**

**Repository:** https://github.com/Mehdi-Jafarian/EECS3311-TEAM1-Project-Phase1

---

## Overview

A full-stack service booking platform that connects clients with professional consultants. Clients browse services, book sessions, process payments, and interact with an AI customer assistant. Consultants manage availability and booking requests. Admins control system policies and consultant approvals.

## Architecture

- **Backend:** Spring Boot 3.2 (Java 17) — REST API with existing GoF design patterns (State, Strategy, Observer, Template Method, Factory Method)
- **Frontend:** React (Vite) — Single-page application with role-based views
- **Database:** PostgreSQL 16 — persistent storage
- **AI Chatbot:** OpenAI GPT-3.5-turbo — informational assistant for clients

## Quick Start

### Prerequisites

- Docker and Docker Compose installed

### Running the System

```bash
# 1. Clone the repository
git clone https://github.com/Mehdi-Jafarian/EECS3311-TEAM1-Project-Phase1.git
cd EECS3311-TEAM1-Project-Phase1

# 2. Create environment file
cp .env.example .env
# Edit .env and set your values (DB_PASSWORD, AI_API_KEY)

# 3. Start everything
docker compose up --build
```

### Accessing the Application

| Service | URL | Description |
|---------|-----|-------------|
| Frontend | http://localhost:3000 | Web UI (client, consultant, admin views) |
| Backend API | http://localhost:8080 | REST API endpoints |
| Database | localhost:5432 | PostgreSQL (internal, not exposed by default) |

### Using the Application

1. Open http://localhost:3000 in your browser
2. Select a role:
   - **Client** — Browse services, book sessions, process payments, chat with AI assistant
   - **Consultant** — Manage availability, accept/reject bookings, complete sessions
   - **Admin** — Approve consultants, configure system policies

### AI Customer Assistant

The AI chatbot is accessible from the **Client** interface:
1. Log in as a Client
2. Click the chat bubble icon (bottom-right corner)
3. Ask questions about services, booking process, payment methods, etc.

Requires `AI_API_KEY` to be set in `.env`. Without it, the chatbot returns a configuration message.

See [AI_CHATBOT_DOCUMENTATION.md](AI_CHATBOT_DOCUMENTATION.md) for full details.

## Environment Variables

Create a `.env` file from `.env.example`:

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_PASSWORD` | Yes | PostgreSQL password |
| `AI_API_KEY` | No* | OpenAI API key for the AI chatbot |
| `AI_PROVIDER` | No | LLM provider (default: `openai`) |

\* The application runs without an API key; the chatbot will return a fallback message.

## Port Mappings

| Port | Service |
|------|---------|
| 3000 | Frontend (Nginx serving React SPA, proxies `/api/*` to backend) |
| 8080 | Backend (Spring Boot REST API) |
| 5432 | PostgreSQL (mapped for debugging; not needed externally) |

## API Endpoints

### Service Catalog
- `GET /api/services` — List all consulting services
- `GET /api/services/{id}/price` — Get effective price

### Clients
- `POST /api/clients` — Register a new client
- `GET /api/clients` — List all clients
- `GET /api/clients/{id}` — Get client details

### Consultants
- `POST /api/consultants` — Register a new consultant
- `GET /api/consultants` — List all consultants
- `GET /api/consultants/{id}` — Get consultant details
- `POST /api/consultants/{id}/timeslots` — Add a time slot
- `GET /api/consultants/{id}/timeslots` — Get time slots

### Bookings
- `POST /api/bookings` — Request a new booking
- `GET /api/bookings/client/{clientId}` — Get client's bookings
- `GET /api/bookings/consultant/{consultantId}` — Get consultant's bookings
- `PUT /api/bookings/{id}/accept?consultantId=...` — Accept booking
- `PUT /api/bookings/{id}/reject?consultantId=...` — Reject booking
- `PUT /api/bookings/{id}/complete?consultantId=...` — Complete booking
- `PUT /api/bookings/{id}/cancel?clientId=...` — Cancel booking

### Payments
- `POST /api/payments` — Process payment
- `GET /api/payments/client/{clientId}` — Payment history
- `POST /api/payment-methods` — Add payment method
- `GET /api/payment-methods/client/{clientId}` — List payment methods
- `DELETE /api/payment-methods/{id}?clientId=...` — Remove payment method

### Admin
- `GET /api/admin/consultants/pending` — List pending consultants
- `PUT /api/admin/consultants/{id}/approve` — Approve consultant
- `PUT /api/admin/consultants/{id}/reject` — Reject consultant
- `GET /api/admin/policy` — Get system policies
- `PUT /api/admin/policy/cancellation` — Set cancellation policy
- `PUT /api/admin/policy/pricing` — Set pricing strategy
- `PUT /api/admin/policy/notifications` — Toggle notifications

### Notifications
- `GET /api/notifications/{recipientId}` — Get notifications

### AI Chatbot
- `POST /api/chat` — Send message to AI assistant

## Project Structure

```
├── backend/
│   ├── src/main/java/com/platform/
│   │   ├── domain/           (Phase 1 — unchanged)
│   │   ├── application/      (Phase 1 + ChatbotService)
│   │   ├── infrastructure/   (Phase 1 + JDBC repositories)
│   │   ├── presentation/
│   │   │   ├── api/          (Phase 2 — REST controllers)
│   │   │   └── *.java        (Phase 1 CLI — kept for reference)
│   │   └── config/           (Phase 2 — Spring configuration)
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── pages/            (client, consultant, admin views)
│   │   ├── components/       (ChatWidget)
│   │   └── api.js            (Axios API client)
│   ├── Dockerfile
│   └── nginx.conf
├── db/
│   └── init.sql              (database schema + seed data)
├── docker-compose.yml
├── .env.example
├── AI_CHATBOT_DOCUMENTATION.md
└── README.md
```

## Phase 2 Changes from Phase 1

### Added (no existing code modified)
- Spring Boot web framework (`pom.xml` updated)
- `PlatformApplication.java` — Spring Boot entry point
- `config/` package — `AppConfig`, `CorsConfig`, `DataSourceConfig`
- `presentation/api/` package — 8 REST controllers + global exception handler
- `ChatbotService` — AI chatbot service in application layer
- `infrastructure/repository/jdbc/` — 8 JDBC repository implementations
- React frontend with all client, consultant, and admin views
- Docker Compose configuration (3 containers)
- Database initialization script

### Not Modified
- All domain classes (`domain/` package)
- All application service classes
- All design pattern implementations (State, Strategy, Observer, Template Method, Factory Method)
- All existing InMemory repository classes (kept for test use)
- Phase 1 CLI classes (kept for reference)

### Minimal Modification
- `Booking.java` — Added `@JsonIgnore` on `stateHandler` field (serialization annotation only, no logic change)
