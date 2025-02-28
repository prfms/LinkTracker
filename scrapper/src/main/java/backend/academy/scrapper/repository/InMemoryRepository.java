package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import org.springframework.data.redis.FallbackExceptionTranslationStrategy;
import java.util.ArrayList;
import java.util.List;

public class InMemoryRepository implements Repository {
    private final List<Link> trackedLinks = new ArrayList<>();
    private long userId;

    @Override
    public long addUser(long userId) {
        this.userId = userId;
        return userId;
    }

    @Override
    public int addLink(Link link) {
        if (!trackedLinks.contains(link)) {
            trackedLinks.add(link);
        }
        return trackedLinks.indexOf(link);
    }

    @Override
    public Link removeLink(String url) {
        for (Link link : trackedLinks) {
            if (link.url().equals(url)) {
                trackedLinks.remove(link);
                return link;
            }
        }
        return null;
    }

    @Override
    public List<Link> getLinks(Long chatId) {
        return new ArrayList<>(trackedLinks);
    }

    @Override
    public boolean containsLink(Link link) {
        return trackedLinks.contains(link);
    }

    @Override
    public long deleteUser(long chatId) {
        this.userId = 0L;
        return chatId;
    }
}
