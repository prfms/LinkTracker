package backend.academy.scrapper.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class StackOverflowClient implements LinkUpdateClient {
    private final WebClient webClient;

    public StackOverflowClient(WebClient.Builder webClientBuilder) {
        this.webClient =
                webClientBuilder.baseUrl("https://api.stackexchange.com/2.3").build();
    }

    public OffsetDateTime getLastUpdated(String questionId) {
        return webClient
                .get()
                .uri("/questions/{id}?order=desc&sort=activity&site=stackoverflow", questionId)
                .retrieve()
                .bodyToMono(StackOverflowResponse.class)
                .map(response -> response.items().getFirst().lastActivityDate())
                .block();
    }

    private record StackOverflowResponse(List<Question> items) {}

    private record Question(@JsonProperty("last_activity_date") OffsetDateTime lastActivityDate) {}
}
