package backend.academy.scrapper;


import backend.academy.DTO;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.clients.GitHubClient;

@Service
public class ScrapperService {
    private final Repository repository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotNotifier botNotifier;

    @Autowired
    public ScrapperService(Repository repository, GitHubClient gitHubClient, StackOverflowClient stackOverflowClient, BotNotifier botNotifier) {
        this.repository = repository;
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
        this.botNotifier = botNotifier;
    }

    public long registerUser(long chatId) {
        return repository.addUser(chatId);
    }

    public long deleteUser(long chatId) {
        return repository.deleteUser(chatId);
    }

    public List<DTO.LinkResponse> getLinks(Long chatId) {
        List<Link> links = repository.getLinks(chatId);
        return links.stream()
            .map(link -> new DTO.LinkResponse(
                link.id(), link.url(), link.tags(), link.filters()))
            .toList();
    }

    public int addLink(Long chatId, DTO.AddLinkRequest link) {
        return repository.addLink(link.link(), chatId, link.tags(), link.filters(), getLastUpdatedTime(link.link()));
    }

    public Link deleteLink(Long chatId, DTO.RemoveLinkRequest link) {
        return repository.removeLink(link.link());
    }

    public void checkAndUpdateLinks() {
        List<Link> links = repository.getAllLinks();

        for (Link link : links) {
            Instant newLastUpdated = getLastUpdatedTime(link.url());

            if (newLastUpdated != null && newLastUpdated.isAfter(link.lastUpdated())) {
                repository.updateLastChecked(link, newLastUpdated);
                sendUpdateToUsers(link);
            }
        }
    }

    private void sendUpdateToUsers(Link link) {
        List<Long> chatIds = repository.findUsersTrackingLink(link.id());
        botNotifier.sendUpdate(new DTO.LinkUpdate(0,link.url(), "Новое обновление", chatIds));
    }

    private Instant getLastUpdatedTime(String url) {
        if (url.contains("github.com")) {
            return gitHubClient.getLastUpdated(url);
        } else if (url.contains("stackoverflow.com")) {
            return stackOverflowClient.getLastUpdated(extractQuestionId(url));
        }
        return Instant.MIN; // если источник не поддерживается
    }

    private String extractQuestionId(String url) {
        return url.replaceAll(".*/questions/(\\d+).*", "$1");
    }
}
