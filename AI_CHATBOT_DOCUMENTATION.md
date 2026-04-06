# AI Customer Assistant — Documentation

## 1. Overview

The AI Customer Assistant is an LLM-powered chatbot integrated into the Service Booking & Consulting Platform. It is accessible from the **Client** interface and answers general questions about the platform — services, booking process, payment methods, cancellation policies, and usage guidance.

The chatbot is **informational only** — it does not perform actions, access private data, or interact with the database.

## 2. Architecture

```
Frontend (Chat Widget) → Backend (POST /api/chat) → Gemini API → Backend → Frontend
```

### Data Flow

1. The client types a question in the chat widget (floating button, bottom-right corner)
2. The frontend sends a `POST /api/chat` request to the backend with the user's message and conversation history
3. The backend's `ChatbotService` constructs a system prompt with public platform information
4. The backend calls the Gemini API (`gemini-2.5-flash`) via `java.net.http.HttpClient`
5. The backend returns the AI's response to the frontend
6. The frontend displays the response in the chat panel

### Key Classes

| Class | Location | Responsibility |
|-------|----------|---------------|
| `ChatbotService` | `com.platform.application` | Builds system prompt, calls Gemini API, returns response |
| `ChatController` | `com.platform.presentation.api` | REST endpoint `POST /api/chat` |
| `ChatWidget` | `frontend/src/components/ChatWidget.jsx` | React chat UI component |

## 3. LLM Provider Configuration

**Provider:** Google Gemini
**Model:** `gemini-2.5-flash`
**Endpoint:** `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent`

### Configuration

Set the following environment variables:

```env
AI_API_KEY=your_gemini_api_key_here
AI_PROVIDER=gemini
```

In Docker Compose, these are read from the `.env` file. Create a `.env` file from `.env.example`:

```bash
cp .env.example .env
# Edit .env and add your Gemini API key
```

If `AI_API_KEY` is not set or empty, the chatbot returns:
> "AI assistant is not configured. Please set the AI_API_KEY environment variable."

## 4. System Prompt Template

The system prompt is dynamically generated at each request using public platform information:

```
You are a helpful customer assistant for a Service Booking & Consulting Platform.

Platform overview:
- Clients can browse consulting services, book sessions with consultants, and process payments.
- Consultants offer professional services such as career coaching, technical architecture reviews, and business strategy sessions.

Available services:
- Career Coaching (Coaching): 60 minutes, $150.00
- Tech Architecture Review (Technical): 90 minutes, $250.00
- Business Strategy (Strategy): 120 minutes, $300.00

Supported payment methods: Credit Card, Debit Card, PayPal, Bank Transfer.

Booking process:
1. Browse available services
2. Select a consultant and an available time slot
3. Submit a booking request
4. Wait for the consultant to accept
5. Process payment
6. Attend the session
7. Session is marked as completed

Current cancellation policy: Free Cancellation (100% refund)
Current pricing strategy: Base Pricing (no discount)

You can only answer questions about the platform. You cannot access personal data, booking details, or perform any actions. If asked about something outside your scope, politely explain that you can only help with general platform questions.
```

## 5. Example Interactions

| User Question | Expected AI Response |
|---------------|---------------------|
| "How do I book a consulting session?" | Explains the 7-step booking process: browse services → select consultant → pick time slot → submit request → wait for acceptance → pay → attend session |
| "What payment methods do you accept?" | Lists: Credit Card, Debit Card, PayPal, Bank Transfer |
| "Can I cancel my booking?" | Explains the current cancellation policy and how to cancel from the booking history page |
| "What types of consulting services are available?" | Lists Career Coaching ($150), Tech Architecture Review ($250), Business Strategy ($300) with durations |
| "How much does a Career Coaching session cost?" | States the base price ($150.00) and mentions the current pricing strategy |
| "What is my booking status?" | Politely explains it cannot access personal data and directs the user to their booking history page |
| "Can you cancel my booking for me?" | Explains it cannot perform actions and guides the user to the cancellation feature in the UI |

## 6. Privacy & Safety Measures

### Data Provided to the AI
- Platform description (static text)
- Service catalog: names, types, durations, and effective prices (public information)
- Supported payment method types (Credit Card, Debit Card, PayPal, Bank Transfer)
- Current cancellation policy name
- Current pricing strategy name
- General booking process description

### Data Explicitly Excluded
- Individual client names, emails, or IDs
- Individual consultant emails or IDs
- Booking records, booking IDs, or booking statuses
- Payment records, transaction IDs, or payment method details
- Notification content
- Any data that could identify a specific user

### How the AI is Prevented from Accessing the Database
- `ChatbotService` does **not** receive any repository as a constructor dependency
- It only receives `ServiceCatalogService` (for listing public service info) and `SystemPolicy` (for policy names)
- There is no code path from the chatbot to any repository or database query

### How the AI is Prevented from Performing Actions
- The `POST /api/chat` endpoint only returns a text response
- `ChatbotService` has no reference to `BookingService`, `PaymentService`, or any action-performing service
- The system prompt explicitly instructs the AI that it cannot perform actions

## 7. API Integration

### Endpoint

```
POST /api/chat
```

### Request Format

```json
{
  "message": "How do I book a consulting session?",
  "conversationHistory": [
    { "role": "user", "content": "previous question" },
    { "role": "assistant", "content": "previous answer" }
  ]
}
```

### Response Format

```json
{
  "response": "To book a consulting session, follow these steps: ..."
}
```

### Gemini API Call

POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={AI_API_KEY}
Headers:
  Content-Type: application/json

Body:
{
  "systemInstruction": {
    "parts": [{ "text": "<system prompt>" }]
  },
  "contents": [
    { "role": "user", "parts": [{ "text": "previous question" }] },
    { "role": "model", "parts": [{ "text": "previous answer" }] },
    { "role": "user", "parts": [{ "text": "<user message>" }] }
  ],
  "generationConfig": {
    "maxOutputTokens": 500,
    "temperature": 0.7
  }
}


## 8. Error Handling

| Scenario | Behavior |
|----------|----------|
| `AI_API_KEY` not set or empty | Returns: "AI assistant is not configured. Please set the AI_API_KEY environment variable." |
| Gemini API returns non-200 status | Logs error, returns: "I'm sorry, I'm currently unable to assist. Please try again later." |
| Network timeout (30s limit) | Catches exception, returns fallback message |
| Any unexpected exception | Catches exception, returns fallback message |
| Gemini API unreachable | Catches exception, returns fallback message |
