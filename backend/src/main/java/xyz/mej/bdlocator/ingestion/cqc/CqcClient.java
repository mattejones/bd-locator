package xyz.mej.bdlocator.ingestion.cqc;

import xyz.mej.bdlocator.ingestion.cqc.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
public class CqcClient {

    private static final String SUBSCRIPTION_HEADER = "Ocp-Apim-Subscription-Key";

    private final WebClient webClient;
    private final String subscriptionKey;

    public CqcClient(
            WebClient.Builder builder,
            @Value("${app.cqc.base-url}") String baseUrl,
            @Value("${app.cqc.subscription-key:}") String subscriptionKey
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.subscriptionKey = subscriptionKey;
    }

    public CqcProviderPageResponse fetchProviderPage(int page, int perPage) {
        return webClient.get()
                .uri(u -> u.path("/providers")
                        .queryParam("page", page)
                        .queryParam("perPage", perPage)
                        .build())
                .header(SUBSCRIPTION_HEADER, subscriptionKey)
                .retrieve()
                .bodyToMono(CqcProviderPageResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .block();
    }

    public CqcProviderPageResponse fetchProviderPageByRegion(int page, int perPage, String region) {
        return webClient.get()
                .uri(u -> u.path("/providers")
                        .queryParam("page", page)
                        .queryParam("perPage", perPage)
                        .queryParam("region", region)
                        .build())
                .header(SUBSCRIPTION_HEADER, subscriptionKey)
                .retrieve()
                .bodyToMono(CqcProviderPageResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .block();
    }

    public Optional<CqcProviderDetail> fetchProviderDetail(String providerId) {
        try {
            return Optional.ofNullable(
                    webClient.get()
                            .uri(u -> u.path("/providers/{id}").build(providerId))
                            .header(SUBSCRIPTION_HEADER, subscriptionKey)
                            .retrieve()
                            .bodyToMono(CqcProviderDetail.class)
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                            .block()
            );
        } catch (Exception e) {
            log.warn("Failed to fetch provider detail for {}: {}", providerId, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<CqcLocationDetail> fetchLocationDetail(String locationId) {
        try {
            return Optional.ofNullable(
                    webClient.get()
                            .uri(u -> u.path("/locations/{id}").build(locationId))
                            .header(SUBSCRIPTION_HEADER, subscriptionKey)
                            .retrieve()
                            .bodyToMono(CqcLocationDetail.class)
                            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                            .block()
            );
        } catch (Exception e) {
            log.warn("Failed to fetch location detail for {}: {}", locationId, e.getMessage());
            return Optional.empty();
        }
    }

    public CqcChangesResponse fetchChanges(OffsetDateTime since, OffsetDateTime until) {
        String start = since.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String end = until.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return webClient.get()
                .uri(u -> u.path("/changes/location")
                        .queryParam("startTimestamp", start)
                        .queryParam("endTimestamp", end)
                        .build())
                .header(SUBSCRIPTION_HEADER, subscriptionKey)
                .retrieve()
                .bodyToMono(CqcChangesResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .block();
    }
}
