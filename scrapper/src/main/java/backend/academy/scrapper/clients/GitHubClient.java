package backend.academy.scrapper.clients;

import backend.academy.scrapper.controller.dto.UpdateInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
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
        this.webClient = webClientBuilder
                .baseUrl(githubBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "token " + githubToken)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .build();
    }

    @Override
    public UpdateInfo getUpdateInfo(String url) {
        try {
            List<String> parts = Splitter.on('/').splitToList(url);
            if (parts.size() < 5) {
                throw new IllegalArgumentException("Некорректный GitHub URL: " + url);
            }

            String owner = parts.get(3).toLowerCase(Locale.ROOT);
            String repo = parts.get(4).toLowerCase(Locale.ROOT);

            // repos/{owner}/{repo}/pulls/{pull_number}
            if (parts.size() >= 7 && "pull".equals(parts.get(5))) {
                int prNumber = Integer.parseInt(parts.get(6));
                PullRequest pr = fetchPullRequest(owner, repo, prNumber);
                OffsetDateTime updatedAt = pr.updatedAt();
                String description = String.format("Pull Request #%d «%s» обновлён", prNumber, pr.title());
                return new UpdateInfo(updatedAt, description);
            }

            // repos/{owner}/{repo}/issues/{issue_number}
            if (parts.size() >= 7 && "issues".equals(parts.get(5))) {
                int issueNumber = Integer.parseInt(parts.get(6));
                Issue issue = fetchIssue(owner, repo, issueNumber);
                OffsetDateTime updatedAt = issue.updatedAt();
                String description =
                        String.format("Issue #%d «%s» (%s) обновлён", issueNumber, issue.title(), issue.state());
                return new UpdateInfo(updatedAt, description);
            }

            // repos/{owner}/{repo}
            RepositoryResponse repoInfo = fetchRepository(owner, repo);
            OffsetDateTime repoUpdated = repoInfo.pushedAt();

            GitHubUpdateDetails latest = fetchLatestUpdate(url);
            OffsetDateTime updatedAt = latest.createdAt().isAfter(repoUpdated) ? latest.createdAt() : repoUpdated;
            String description = String.format("Репозиторий %s/%s был обновлён", owner, repo);
            return new UpdateInfo(updatedAt, description);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения даты обновления GitHub ресурса: " + url, e);
        }
    }

    private RepositoryResponse fetchRepository(String owner, String repo) {
        return webClient
                .get()
                .uri("/repos/{owner}/{repo}", owner, repo)
                .retrieve()
                .bodyToMono(RepositoryResponse.class)
                .block();
    }

    private PullRequest fetchPullRequest(String owner, String repo, int number) {
        return webClient
                .get()
                .uri("/repos/{owner}/{repo}/pulls/{number}", owner, repo, number)
                .retrieve()
                .bodyToMono(PullRequest.class)
                .block();
    }

    private Issue fetchIssue(String owner, String repo, int number) {
        return webClient
                .get()
                .uri("/repos/{owner}/{repo}/issues/{number}", owner, repo, number)
                .retrieve()
                .bodyToMono(Issue.class)
                .block();
    }

    private GitHubUpdateDetails fetchLatestUpdate(String repoUrl) {
        String[] parts = repoUrl.split("/", 5);
        String owner = parts[3];
        String repo = parts[4];

        List<PullRequest> prs = fetchPullRequests(owner, repo);
        List<Issue> issues = fetchIssues(owner, repo);

        // фильтруем "чистые" Issue (без PR)
        List<Issue> pureIssues =
                issues.stream().filter(i -> i.pullRequest == null).collect(Collectors.toList());

        return getLatestUpdate(prs, pureIssues);
    }

    private List<PullRequest> fetchPullRequests(String owner, String repo) {
        PullRequest[] prs = webClient
                .get()
                .uri("/repos/{owner}/{repo}/pulls?state=all&sort=created&direction=desc", owner, repo)
                .retrieve()
                .bodyToMono(PullRequest[].class)
                .block();
        return prs != null ? Arrays.asList(prs) : List.of();
    }

    private List<Issue> fetchIssues(String owner, String repo) {
        Issue[] issues = webClient
                .get()
                .uri("/repos/{owner}/{repo}/issues?state=all&sort=created&direction=desc", owner, repo)
                .retrieve()
                .bodyToMono(Issue[].class)
                .block();
        return issues != null ? Arrays.asList(issues) : List.of();
    }

    private GitHubUpdateDetails getLatestUpdate(List<PullRequest> prs, List<Issue> issues) {
        OffsetDateTime latestTime = OffsetDateTime.MIN;
        GitHubUpdateDetails latest = new GitHubUpdateDetails("None", "", "", OffsetDateTime.MIN, "");

        for (PullRequest pr : prs) {
            if (pr.createdAt() != null && pr.createdAt().isAfter(latestTime)) {
                latestTime = pr.createdAt();
                latest = new GitHubUpdateDetails(
                        "Pull Request", pr.title(), pr.user().login(), pr.createdAt(), truncatePreview(pr.body()));
            }
        }

        for (Issue issue : issues) {
            if (issue.createdAt() != null && issue.createdAt().isAfter(latestTime)) {
                latestTime = issue.createdAt();
                latest = new GitHubUpdateDetails(
                        "Issue", issue.title(), issue.user().login(), issue.createdAt(), truncatePreview(issue.body()));
            }
        }

        return latest;
    }

    private String truncatePreview(String text) {
        if (text == null) return "";
        text = text.strip();
        return text.length() > 200 ? text.substring(0, 200) : text;
    }

    public record RepositoryResponse(
            @JsonProperty("id") long id,
            @JsonProperty("name") String name,
            @JsonProperty("pushed_at") OffsetDateTime pushedAt) {}

    public record PullRequest(
            @JsonProperty("title") String title,
            @JsonProperty("body") String body,
            @JsonProperty("user") User user,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt) {}

    public record Issue(
            @JsonProperty("title") String title,
            @JsonProperty("body") String body,
            @JsonProperty("user") User user,
            @JsonProperty("created_at") OffsetDateTime createdAt,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            @JsonProperty("state") String state,
            @JsonProperty("pull_request") Map<String, Object> pullRequest) {}

    public record User(@JsonProperty("login") String login) {}

    public record GitHubUpdateDetails(
            String type, String title, String username, OffsetDateTime createdAt, String preview) {}
}
