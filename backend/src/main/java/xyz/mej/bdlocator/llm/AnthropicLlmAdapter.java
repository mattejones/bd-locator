package xyz.mej.bdlocator.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
                Your task is to select the most relevant terms from the EXACT lists below \
                that match their description. Only use terms from these lists — do not invent new ones.

                CQC service types (use substrings or exact values):
                - Accommodation for persons who require nursing or personal care
                - Community based activities for people with mental health needs
                - Homecare agencies
                - Hospice
                - Nursing care
                - Nursing homes
                - Personal care
                - Rehabilitation (illness/injury)
                - Residential homes
                - Shared lives
                - Supported living
                - Supported housing

                CQC user bands (use substrings or exact values):
                - Dementia
                - Older Adults
                - Caring for adults over 65 yrs
                - Caring for adults under 65 yrs
                - Learning disabilities
                - Mental health conditions
                - Physical disabilities
                - Sensory impairment
                - Substance misuse problems

                Return ONLY a JSON array of 3 to 8 lowercase keyword strings chosen from \
                the values above. These will be used for substring matching so shorter \
                terms that appear within longer ones are preferred. \
                No explanation, no preamble, no markdown. \
                Example: ["dementia", "older adults", "nursing homes", "personal care"]
                """;
    }
}
