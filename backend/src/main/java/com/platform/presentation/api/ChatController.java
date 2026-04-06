package com.platform.presentation.api;

import com.platform.application.ChatbotService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatbotService chatbotService;

    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @SuppressWarnings("unchecked")
    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");
        List<Map<String, String>> history = (List<Map<String, String>>) body.get("conversationHistory");
        String response = chatbotService.chat(message, history);
        return Map.of("response", response);
    }
}
