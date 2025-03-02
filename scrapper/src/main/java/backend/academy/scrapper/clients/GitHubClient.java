package backend.academy.scrapper.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Instant;

@Service
public class GitHubClient {
    private final WebClient webClient;

    public GitHubClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com").build();
    }

    public Instant getLastUpdated(String repoUrl) {
        String apiUrl = repoUrl.replace("https://github.com/", "/repos/");
        return webClient.get()
            .uri(apiUrl)
            .retrieve()
            .bodyToMono(GitHubResponse.class)
            .map(GitHubResponse::updatedAt)
            .block();
    }

    private record GitHubResponse(@JsonProperty("updated_at") Instant updatedAt) {}
}
