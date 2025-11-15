package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.LinkDto;
import backend.academy.scrapper.model.User;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class OrmRepository implements CommonRepository {
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;

    @Override
    public long addUser(long userId) {
        if (userRepository.existsById(userId)) {
            return userId;
        }
        User user = new User();
        user.id(userId);
        return userRepository.save(user).id();
    }

    @Override
    public int addLink(String url, long chatId, OffsetDateTime lastUpdated) {
        User user = userRepository.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with chatId = " + chatId));

        Link link = new Link();
        link.url(url);
        link.chatId(chatId);
        link.lastUpdated(lastUpdated);
        link.user(user);

        Link saved = linkRepository.save(link);
        return saved.id();
    }

    @Override
    public LinkDto removeLink(String url, long chatId) {
        Optional<Link> linkOpt = linkRepository.findByUrlAndChatId(url, chatId);
        if (linkOpt.isEmpty()) return null;

        Link link = linkOpt.get();
        linkRepository.delete(link);
        return toDto(link);
    }

    @Override
    public List<LinkDto> getLinks(long chatId) {
        return linkRepository.findByChatId(chatId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public boolean containsLink(String url) {
        return linkRepository.existsByUrl(url);
    }

    @Override
    public List<LinkDto> getAllLinks() {
        return linkRepository.findAll()
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public long deleteUser(long chatId) {
        Optional<User> userOpt = userRepository.findById(chatId);
        if (userOpt.isEmpty()) return 0;

        userRepository.delete(userOpt.get());
        return chatId;
    }

    @Override
    public void updateLastChecked(LinkDto dto, OffsetDateTime lastUpdated) {
        linkRepository.findById(dto.id())
            .ifPresent(link -> {
                link.lastUpdated(lastUpdated);
                linkRepository.save(link);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findUsersTrackingLink(int id) {
        Optional<Link> linkOpt = linkRepository.findById(id);
        return linkOpt.map(link -> List.of(link.user().id())).orElseGet(List::of);

    }

    @Override
    public boolean ifUserExists(long id) {
        return userRepository.existsById(id);
    }

    private LinkDto toDto(Link link) {
        return new LinkDto(
            link.id(),
            link.url(),
            link.chatId(),
            link.lastUpdated()
        );
    }
}
