package com.platform.application;

import com.platform.domain.ConsultingService;
import com.platform.domain.policy.SystemPolicy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatbotService {

    private final String apiKey;
    private final ServiceCatalogService catalogService;
    private final SystemPolicy systemPolicy;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String GEMINI_MODEL = "gemini-2.5-flash";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent";
    private static final String FALLBACK_NO_KEY = "AI assistant is not configured. Please set the AI_API_KEY environment variable.";
    private static final String FALLBACK_ERROR = "I'm sorry, I'm currently unable to assist. Please try again later.";

    public ChatbotService(String apiKey, ServiceCatalogService catalogService, SystemPolicy systemPolicy) {
        this.apiKey = apiKey;
        this.catalogService = catalogService;
        this.systemPolicy = systemPolicy;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String chat(String userMessage, List<Map<String, String>> conversationHistory) {
        if (apiKey == null || apiKey.isBlank()) {
            return FALLBACK_NO_KEY;
        }

        try {
            String systemPrompt = buildSystemPrompt();

            List<Map<String, Object>> contents = new ArrayList<>();

            if (conversationHistory != null) {
                for (Map<String, String> msg : conversationHistory) {
                    String role = msg.get("role");
                    String text = msg.get("content");
                    if (role != null && text != null) {
                        String geminiRole = "assistant".equals(role) ? "model" : "user";
                        contents.add(Map.of(
                                "role", geminiRole,
                                "parts", List.of(Map.of("text", text))
                        ));
                    }
                }
            }

            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", userMessage))
            ));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", contents);
            requestBody.put("systemInstruction", Map.of(
                    "parts", List.of(Map.of("text", systemPrompt))
            ));
            requestBody.put("generationConfig", Map.of(
                    "maxOutputTokens", 500,
                    "temperature", 0.7
            ));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            String url = GEMINI_URL + "?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("[CHATBOT] Gemini API error: " + response.statusCode() + " - " + response.body());
                return FALLBACK_ERROR;
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

        } catch (Exception e) {
            System.err.println("[CHATBOT] Error calling Gemini: " + e.getMessage());
            return FALLBACK_ERROR;
        }
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful customer assistant for a Service Booking & Consulting Platform.\n\n");
        sb.append("Platform overview:\n");
        sb.append("- Clients can browse consulting services, book sessions with consultants, and process payments.\n");
        sb.append("- Consultants offer professional services such as career coaching, technical architecture reviews, and business strategy sessions.\n\n");

        sb.append("Available services:\n");
        try {
            List<ConsultingService> services = catalogService.listServices();
            for (ConsultingService s : services) {
                double price = systemPolicy.getPricingStrategy().calculatePrice(s);
                sb.append(String.format("- %s (%s): %d minutes, $%.2f\n",
                        s.getName(), s.getType(), s.getDurationMinutes(), price));
            }
        } catch (Exception e) {
            sb.append("- Service information temporarily unavailable.\n");
        }

        sb.append("\nSupported payment methods: Credit Card, Debit Card, PayPal, Bank Transfer.\n\n");

        sb.append("Booking process:\n");
        sb.append("1. Browse available services\n");
        sb.append("2. Select a consultant and an available time slot\n");
        sb.append("3. Submit a booking request\n");
        sb.append("4. Wait for the consultant to accept\n");
        sb.append("5. Process payment\n");
        sb.append("6. Attend the session\n");
        sb.append("7. Session is marked as completed\n\n");

        sb.append("Current cancellation policy: ").append(systemPolicy.getCancellationPolicy().getName()).append("\n");
        sb.append("Current pricing strategy: ").append(systemPolicy.getPricingStrategy().getName()).append("\n\n");

        sb.append("You can only answer questions about the platform. You cannot access personal data, booking details, or perform any actions. ");
        sb.append("If asked about something outside your scope, politely explain that you can only help with general platform questions.");

        return sb.toString();
    }
}
