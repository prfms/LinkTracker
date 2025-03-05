package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.time.OffsetDateTime;
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
    public int addLink(String url, long chatId, List<String> tags, List<String> filters, OffsetDateTime lastUpdated) {
        for (int i = 0; i < trackedLinks.size(); i++) {
            Link link = trackedLinks.get(i);
            if (link.url().equals(url)
                    && link.chatId() == chatId
                    && link.tags().equals(tags)
                    && link.filters().equals(filters)) {
                return i;
            }
        }

        Link addedLink = new Link(trackedLinks.size(), url, chatId, tags, filters, lastUpdated);
        trackedLinks.add(addedLink);
        return trackedLinks.size() - 1;
    }

    @Override
    public Link removeLink(String url, long chatId) {
        for (Link link : trackedLinks) {
            if (link.url().equals(url)) {
                trackedLinks.remove(link);
                return link;
            }
        }
        return null;
    }

    @Override
    public List<Link> getLinks(long chatId) {
        return new ArrayList<>(trackedLinks);
    }

    @Override
    public boolean containsLink(String url) {
        for (Link link : trackedLinks) {
            if (link.url().equals(url)) {
                return true;
            }
        }
        return false;
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
    public void updateLastChecked(Link link, OffsetDateTime lastUpdated) {
        link.lastUpdated(lastUpdated);
    }

    @Override
    public List<Long> findUsersTrackingLink(int id) {
        return List.of(userId);
    }

    @Override
    public boolean ifUserExists(long id) {
        return id == userId;
    }
}
