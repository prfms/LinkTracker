package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

public interface Repository {
    long addUser(long userId);
    int addLink(String url, long chatId, List<String> tags, List<String> filters, OffsetDateTime lastUpdated);
    Link removeLink(String url);
    List<Link> getLinks(Long chatId);
    boolean containsLink(Link link);
    List<Link> getAllLinks();
    long deleteUser(long chatId);

    void updateLastChecked(Link url, OffsetDateTime lastUpdated);

    List<Long> findUsersTrackingLink(int id);
}
