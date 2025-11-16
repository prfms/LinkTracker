package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<Link, Integer> {
    Optional<Link> findByUrlAndChatId(String url, Long chatId);

    List<Link> findByChatId(Long chatId);

    boolean existsByUrl(String url);

    Optional<Link> findByUrl(String url);
}
