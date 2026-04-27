package xyz.mej.bdlocator.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class AnthropicLlmAdapter implements LlmAdapter {

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com";
    private static final String MODEL = "claude-haiku-4-5-20251001";
    private static final String API_VERSION = "2023-06-01";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public AnthropicLlmAdapter(WebClient.Builder builder, String apiKey) {
        this.webClient = builder.baseUrl(ANTHROPIC_API_URL).build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }

    @Override
    public Set<String> expandIcp(String icpDescription) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", MODEL,
                    "max_tokens", 256,
                    "system", systemPrompt(),
                    "messages", List.of(
                            Map.of("role", "user", "content", icpDescription)
                    )
            );

            String responseBody = webClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", API_VERSION)
                    .header("content-type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseKeywords(responseBody);

        } catch (Exception e) {
            log.warn("ICP expansion failed — returning empty keyword set: {}", e.getMessage());
            return Set.of();
        }
    }

    private Set<String> parseKeywords(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String text = root
                .path("content")
                .get(0)
                .path("text")
                .asText();

        // Strip any accidental markdown fencing
        text = text.replaceAll("```json|```", "").trim();

        JsonNode array = objectMapper.readTree(text);
        Set<String> keywords = new HashSet<>();
        array.forEach(node -> keywords.add(node.asText().toLowerCase()));
        return keywords;
    }

    private String systemPrompt() {
        return """
                You are a care sector data specialist. A business development professional \
                will describe their ideal care provider partner in natural language. \
                Your task is to expand that description into relevant search keywords \
                drawn from CQC (Care Quality Commission) taxonomy.

                Known CQC service types:
                - accommodation for persons who require nursing or personal care
                - personal care
                - nursing care
                - community based activities for people with mental health needs
                - hospice care
                - rehabilitation services
                - treatment of disease disorder or injury
                - diagnostic and screening services

                Known CQC user bands:
                - older adults
                - dementia
                - physical disabilities
                - mental health
                - learning disabilities
                - substance misuse
                - eating disorders

                Return ONLY a JSON array of 3 to 8 lowercase keyword strings that would \
                match relevant CQC providers. No explanation, no preamble, no markdown. \
                Example output: ["dementia", "older adults", "nursing care", "accommodation"]
                """;
    }
}
