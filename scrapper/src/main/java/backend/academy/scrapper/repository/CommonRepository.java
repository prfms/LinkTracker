package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.LinkDto;
import java.time.OffsetDateTime;
import java.util.List;

public interface CommonRepository {
    long addUser(long userId);

    int addLink(String url, long chatId, OffsetDateTime lastUpdated);

    LinkDto removeLink(String url, long chatId);

    List<LinkDto> getLinks(long chatId);

    boolean containsLink(String url);

    List<LinkDto> getAllLinks();

    long deleteUser(long chatId);

    void updateLastChecked(LinkDto url, OffsetDateTime lastUpdated);

    List<Long> findUsersTrackingLink(int id);

    boolean ifUserExists(long id);
}
