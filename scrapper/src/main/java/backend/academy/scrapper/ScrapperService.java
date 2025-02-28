package backend.academy.scrapper;


import backend.academy.DTO;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScrapperService {
    private final Repository repository;

    @Autowired
    public ScrapperService(Repository repository) {
        this.repository = repository;
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
        return repository.addLink(new Link(0, link.link(), chatId, link.tags(), link.filters()));
    }

    public Link deleteLink(Long chatId, DTO.RemoveLinkRequest link) {
        return repository.removeLink(link.link());
    }
}
