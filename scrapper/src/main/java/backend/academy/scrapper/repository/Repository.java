package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.util.List;

public interface Repository {
    long addUser(long userId);
    int addLink(Link link);
    Link removeLink(String url);
    List<Link> getLinks(Long chatId);
    boolean containsLink(Link link);

    long deleteUser(long chatId);
}
