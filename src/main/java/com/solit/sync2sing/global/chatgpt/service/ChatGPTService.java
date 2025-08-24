package com.solit.sync2sing.global.chatgpt.service;

import com.solit.sync2sing.global.chatgpt.dto.ChatGPTRequest;
import com.solit.sync2sing.global.chatgpt.dto.ChatGPTResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatGPTService {

    private final WebClient webClient;

    @Value("${openai.api.url}")
    private String OpenaiApiUrl;
    @Value("${openai.api.key}")
    private String OpenaiApiKey;

    public String askToGpt(String userPrompt) {
        return askToGpt(userPrompt, "");
    }

    public String askToGpt(String userPrompt, String developerPrompt) {
        ChatGPTRequest.Message developerMessage = new ChatGPTRequest.Message();
        developerMessage.setRole("developer");
        developerMessage.setContent(developerPrompt);

        ChatGPTRequest.Message userMessage = new ChatGPTRequest.Message();
        userMessage.setRole("user");
        userMessage.setContent(userPrompt);

        List<ChatGPTRequest.Message> messages = new ArrayList<>();
        messages.add(developerMessage);
        messages.add(userMessage);

        ChatGPTRequest chatRequest = new ChatGPTRequest();
        chatRequest.setModel("gpt-4.1-nano");
        chatRequest.setMessages(messages);

        ChatGPTResponse chatResponse = webClient.post()
                .uri(OpenaiApiUrl + "/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + OpenaiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(chatRequest)
                .retrieve()
                .bodyToMono(ChatGPTResponse.class)
                .block();

        if (chatResponse == null || chatResponse.getChoices() == null || chatResponse.getChoices().isEmpty()) {
            return "No response";
        }
        return chatResponse.getChoices().get(0).getMessage().getContent();
    }
}
