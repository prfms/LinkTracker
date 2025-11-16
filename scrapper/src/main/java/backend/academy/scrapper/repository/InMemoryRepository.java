package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.LinkDto;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryRepository implements CommonRepository {
    private final List<LinkDto> trackedLinks = new ArrayList<>();
    private long userId;

    @Override
    public long addUser(long userId) {
        this.userId = userId;
        return userId;
    }

    @Override
    public int addLink(String url, long chatId, OffsetDateTime lastUpdated) {
        for (int i = 0; i < trackedLinks.size(); i++) {
            LinkDto link = trackedLinks.get(i);
            if (link.url().equals(url) && link.chatId() == chatId) {
                return i;
            }
        }

        LinkDto addedLink = new LinkDto(trackedLinks.size(), url, chatId, lastUpdated);
        trackedLinks.add(addedLink);
        return trackedLinks.size() - 1;
    }

    @Override
    public LinkDto removeLink(String url, long chatId) {
        for (LinkDto link : trackedLinks) {
            if (link.url().equals(url)) {
                trackedLinks.remove(link);
                return link;
            }
        }
        return null;
    }

    @Override
    public List<LinkDto> getLinks(long chatId) {
        return new ArrayList<>(trackedLinks);
    }

    @Override
    public boolean containsLink(String url) {
        for (LinkDto link : trackedLinks) {
            if (link.url().equals(url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<LinkDto> getAllLinks() {
        return trackedLinks;
    }

    @Override
    public long deleteUser(long chatId) {
        this.userId = 0L;
        return chatId;
    }

    @Override
    public void updateLastChecked(LinkDto link, OffsetDateTime lastUpdated) {
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
