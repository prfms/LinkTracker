package backend.academy.scrapper.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GitHubClient {
    private final WebClient webClient;

    public GitHubClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com").build();
    }

    public OffsetDateTime getLastUpdated(String url) {
        String[] partsOfUrl = url.split("/", 5);

        String owner = partsOfUrl[3];
        String repo = partsOfUrl[4];

        RepositoryResponse updateFromSite = getRepository(owner, repo);
        return updateFromSite.pushedAt();
    }

    private RepositoryResponse getRepository(String user, String repository) {
        return webClient
                .get()
                .uri("/repos/{user}/{repository}", user, repository)
                .retrieve()
                .bodyToMono(RepositoryResponse.class)
                .block();
    }

    public record RepositoryResponse(
            @JsonProperty("id") long id,
            @JsonProperty("name") String repositoryName,
            @JsonProperty("pushed_at") OffsetDateTime pushedAt) {}
}
