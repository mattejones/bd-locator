package xyz.mej.bdlocator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import xyz.mej.bdlocator.llm.AnthropicLlmAdapter;
import xyz.mej.bdlocator.llm.LlmAdapter;

@Slf4j
@Configuration
public class LlmConfig {

    @Value("${app.llm.provider:anthropic}")
    private String provider;

    @Value("${app.llm.api-key:}")
    private String apiKey;

    @Bean
    public LlmAdapter llmAdapter(WebClient.Builder builder) {
        return switch (provider.toLowerCase()) {
            case "anthropic" -> {
                if (apiKey.isBlank()) {
                    log.warn("LLM provider is 'anthropic' but no API key configured — " +
                             "ICP expansion will return empty keyword sets");
                }
                yield new AnthropicLlmAdapter(builder, apiKey);
            }
            default -> {
                log.warn("Unknown LLM provider '{}' — ICP expansion disabled", provider);
                yield description -> java.util.Set.of();
            }
        };
    }
}
