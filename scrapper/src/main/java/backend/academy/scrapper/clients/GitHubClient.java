package backend.academy.scrapper.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GitHubClient implements LinkUpdateClient {
    private final WebClient webClient;

    @Value("${app.github-base-url:https://api.github.com}")
    private String githubBaseUrl = "https://api.github.com";

    public GitHubClient(WebClient.Builder webClientBuilder, @Value("${app.github-token}") String githubToken) {
        this.webClient = webClientBuilder.baseUrl(githubBaseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "token " + githubToken)
            .build();
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
