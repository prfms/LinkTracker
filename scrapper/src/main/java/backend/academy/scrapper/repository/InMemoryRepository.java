package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.time.Instant;
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
    public int addLink(String url, long chatId, List<String> tags, List<String> filters, Instant lastUpdated) {
        for (Link link : trackedLinks) {
            if (link.url().equals(url) && link.chatId() == chatId && link.tags().equals(tags) && link.filters().equals(filters)) {
                return trackedLinks.indexOf(link);
            } else {
                Link addedLink = new Link(trackedLinks.size(), url, chatId, tags, filters, lastUpdated);
                trackedLinks.add(addedLink);
                return trackedLinks.indexOf(addedLink);
            }
        }
        return -1;
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
    public List<Link> getAllLinks() {
        return trackedLinks;
    }

    @Override
    public long deleteUser(long chatId) {
        this.userId = 0L;
        return chatId;
    }

    @Override
    public void updateLastChecked(Link link, Instant lastUpdated) {
        link.lastUpdated(lastUpdated);
    }

    @Override
    public List<Long> findUsersTrackingLink(int id) {
        return List.of(userId);
    }
}
