# EECS3311 — Phase 1: Service Booking & Consulting Platform

**Course:** EECS 3311 – Software Design  
**Team:** Mehdi Jafarian, Naeesha Puri, Spence Hashemi  
**GitHub:** https://github.com/Mehdi-Jafarian/EECS3311-TEAM1-Project-Phase1

Java 17 (Maven) implementation including PlantUML diagrams, CLI frontend, JUnit 5 tests, and Docker support. No frameworks, no database — all persistence is in-memory.

---

## Project Structure

```
diagrams/
  puml/              — PlantUML source files
  pdf/               — Exported PDFs
backend/             — Maven project (Java 17) + JUnit 5 tests
Dockerfile
docker-compose.yml
README.md
```

---

## Architecture

Clean 4-layer structure:

```
com.platform
├── domain/          — Entities, enums, exceptions
│   ├── state/       — State pattern: booking lifecycle
│   ├── policy/      — Strategy pattern: cancellation & pricing
│   └── exception/   — Custom unchecked domain exceptions
├── application/     — Use-case services
│   └── payment/     — Template Method + Factory Method: payment processors
├── infrastructure/  — In-memory repositories, TimeProvider
└── presentation/    — CLI menus (thin, delegates to services)
```

| Layer | Key Classes |
|---|---|
| Domain | `Booking`, `Client`, `Consultant`, `ConsultingService`, `TimeSlot`, `Payment`, `Notification`, `PaymentMethod` |
| Application | `BookingService`, `PaymentService`, `AdminService`, `ConsultantService`, `ClientService`, `ServiceCatalogService`, `NotificationService` |
| Infrastructure | `InMemory*Repository` classes, `SystemTimeProvider`, `FixedTimeProvider` |
| Presentation | `Application`, `MainMenu`, `ClientMenu`, `ConsultantMenu`, `AdminMenu` |

---

## Use Cases (UC1–UC12)

**Client:** UC1 Browse services · UC2 Request booking · UC3 Cancel booking · UC4 View booking history · UC5 Process payment · UC6 Manage payment methods · UC7 View payment history

**Consultant:** UC8 Manage availability · UC9 Accept/reject bookings · UC10 Complete booking (only if Paid)

**Admin:** UC11 Approve/reject consultant registration · UC12 Configure cancellation policy, pricing strategy, notifications

---

## Booking Lifecycle

States: `REQUESTED` → `PENDING_PAYMENT` → `PAID` → `COMPLETED`, with branches to `REJECTED` and `CANCELLED`.

- Consultant accepting a booking → **PENDING_PAYMENT**
- Successful payment → **PAID**
- Completing a booking is only permitted from **PAID**

---

## Payment Processing

**Supported methods:** Credit Card, Debit Card, PayPal, Bank Transfer

**Validation rules:**

| Method | Rules |
|---|---|
| Credit / Debit Card | 16-digit number, future expiry date, 3–4 digit CVV |
| PayPal | Valid email format |
| Bank Transfer | Account number: 8–17 digits. Routing number: exactly 9 digits |

**Payment flow:**
1. Client selects a booking awaiting payment
2. Selects or adds a payment method
3. Details validated
4. Payment simulated (2.5s delay)
5. Unique transaction ID generated (`TXN-<UUID>`)
6. Payment recorded; booking set to **PAID**
7. Confirmation notification generated

---

## GoF Design Patterns

### 1. State — Booking Lifecycle
**Problem:** 7 states with strict transition rules — without State, services would need sprawling `if/switch` blocks and invalid transitions would be easy to miss.  
**Solution:** Each state is a `BookingStateHandler` implementation. `Booking` delegates all transitions to its current handler; invalid calls throw `InvalidBookingStateException` automatically.  
**Participants:** `BookingStateHandler`, `RequestedState`, `PendingPaymentState`, `PaidState`, `RejectedState`, `CancelledState`, `CompletedState`, `Booking`

### 2. Strategy — Cancellation & Pricing
**Problem:** Admin must swap cancellation rules and pricing at runtime without touching service code.  
**Solution:** `CancellationPolicy` and `PricingStrategy` are interfaces held by `SystemPolicy`. Admin swaps them via `AdminService`.  
**Cancellation:** `FreeCancellationPolicy`, `PartialRefundPolicy`, `NoCancellationRefundPolicy`  
**Pricing:** `BasePricingStrategy`, `DiscountedPricingStrategy`  
**Participants:** `CancellationPolicy`, `PricingStrategy`, `SystemPolicy`, `AdminService`

### 3. Observer — Notifications
**Problem:** `BookingService` and `PaymentService` must trigger notifications without being coupled to notification logic.  
**Solution:** Services publish `BookingEvent` objects to registered `BookingEventObserver` instances. `NotificationObserver` handles delivery via `NotificationService` (stores + prints to console).  
**Participants:** `BookingEventObserver`, `BookingEvent`, `NotificationObserver`, `NotificationService`

### 4. Template Method — Payment Processing Algorithm
**Problem:** All payment processors share the same processing steps (validate → delay → generate ID → create record) but differ only in validation logic.  
**Solution:** `PaymentProcessor.process()` is `final` and defines the algorithm skeleton. The `validate()` step is `abstract`, implemented differently by each subclass.  
**Participants:** `PaymentProcessor` (abstract), `CreditCardProcessor`, `DebitCardProcessor`, `PayPalProcessor`, `BankTransferProcessor`

### 5. Factory Method — Payment Processor Creation
**Problem:** `PaymentService` should not be coupled to every concrete processor type.  
**Solution:** `PaymentProcessorFactory.create(PaymentType)` returns the correct processor. `delayMs` is injectable so tests pass `0` to skip the simulated delay.  
**Participants:** `PaymentProcessorFactory`, `PaymentProcessor` subclasses

---

## UML Diagrams

Sources in `diagrams/puml/`, pre-rendered PDFs in `diagrams/pdf/`.

**To re-render:**
- **Online:** Paste `.puml` content at [plantuml.com/plantuml](https://www.plantuml.com/plantuml)
- **VS Code:** PlantUML extension → open `.puml` → `Option+D` (Mac) / `Alt+D` (Win/Linux)
- **JAR:** `java -jar plantuml.jar diagrams/puml/use_case_diagram.puml`

---

## Running Locally (Maven)

**Prerequisites:** Java 17+, Maven 3.8+

```bash
cd backend
mvn package -DskipTests
java -jar target/service-booking-platform-1.0.0.jar
```

---

## Running with Docker

**Prerequisites:** Docker Desktop

```bash
# Option A — Compose (recommended)
docker compose up --build

# Option B — Direct
docker build -t booking-platform .
docker run -it --rm booking-platform
```

> `-it` is required — without it stdin is closed and the CLI won't accept input.

---

## Running Tests

```bash
cd backend
mvn test
```

| Test File | Covers |
|---|---|
| `BookingStateTransitionTest` | Valid and invalid state transitions |
| `CancellationPolicyTest` | All three cancellation policy behaviours |
| `ConsultantAdminTest` | Consultant approval/rejection, admin policy config |
| `PaymentValidationTest` | Validation rules for all four payment methods |
| `PaymentFlowTest` | End-to-end: validation → delay → transaction ID → booking Paid |

---

## CLI Demo (Recommended Order for Grading)

**1. Admin (UC11, UC12)**
- Approve a consultant registration
- Set cancellation policy (free / partial / none)
- Set pricing strategy (base / discounted)
- Toggle notifications on/off

**2. Consultant (UC8, UC9, UC10)**
- Add available time slots
- View and accept/reject incoming booking requests
- Mark a booking as completed (after payment)

**3. Client (UC1–UC7)**
- Browse services
- Request a booking (approved consultant + available slot)
- Add a payment method and process payment
- View booking history and payment history
- Cancel a booking (observe refund behaviour per active policy)
- View notifications

---

## Team Contributions

| Contributor | Contributions |
|---|---|
| Mehdi Jafarian | Project setup, PlantUML diagrams, booking state machine, in-memory repositories, payment processors, CLI menus, unit tests, Docker |
| Naeesha Puri | Observer pattern, `AdminService`, `ClientService`, `ConsultantService`, `ServiceCatalogService`, exception hierarchy, payment domain models |
| Spence Hashemi | Core domain entities, repository interfaces, `TimeProvider` abstraction, `pom.xml`, Docker files |

> Full commit history available in the GitHub repository above.

---

## Notes & Assumptions

- Persistence is simulated with in-memory repositories (no database).
- Notifications are stored in-memory and printed to console (no email or external services).
- Payment processing delay is 2.5 seconds in production; injectable as `0` for tests.
- Bank transfer format: account number 8–17 digits, routing number exactly 9 digits.
