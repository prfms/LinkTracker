package backend.academy.scrapper.service;

import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.controller.dto.AddLinkRequestDto;
import backend.academy.scrapper.controller.dto.LinkUpdateDto;
import backend.academy.scrapper.controller.dto.RemoveLinkRequestDto;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.model.LinkDto;
import backend.academy.scrapper.repository.CommonRepository;
import backend.academy.scrapper.scheduler.BotNotifier;
import backend.academy.scrapper.controller.dto.LinkResponseDto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScrapperService {
    private final CommonRepository repository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotNotifier botNotifier;
    private static final Logger LOGGER = Logger.getLogger(ScrapperService.class.getName());

    @Autowired
    public ScrapperService(
            CommonRepository repository,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient,
            BotNotifier botNotifier) {
        this.repository = repository;
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
        this.botNotifier = botNotifier;
    }

    public long registerUser(long chatId) {
        return repository.addUser(chatId);
    }

    public long deleteUser(long chatId) {
        return repository.deleteUser(chatId);
    }

    public List<LinkResponseDto> getLinks(long chatId) {
        if (repository.ifUserExists(chatId)) {

            List<LinkDto> links = repository.getLinks(chatId);
            return links.stream()
                    .map(link -> new LinkResponseDto(link.id(), link.url(), link.tags(), link.filters()))
                    .toList();
        } else {
            throw new NotFoundException("Пользователь с ID " + chatId + " не найден.");
        }
    }

    public int addLink(long chatId, AddLinkRequestDto link) {
        return repository.addLink(link.link(), chatId, link.tags(), link.filters(), getLastUpdatedTime(link.link()));
    }

    public LinkDto deleteLink(long chatId, RemoveLinkRequestDto link) {
        if (repository.containsLink(link.link())) {
            return repository.removeLink(link.link(), chatId);
        } else {
            throw new NotFoundException("Ссылка " + link.link() + " у пользователя " + chatId + " не найдена.");
        }
    }

    public void checkAndUpdateLinks() {
        List<LinkDto> links = repository.getAllLinks();
        LOGGER.info("Проверка " + links.size() + " ссылок на обновления");
        for (LinkDto link : links) {
            OffsetDateTime newLastUpdated = getLastUpdatedTime(link.url());
            LOGGER.info("Ссылка: " + link.url() + " | Старый lastUpdated: " + link.lastUpdated()
                    + " | Новый lastUpdated: " + newLastUpdated);

            if (newLastUpdated != null && newLastUpdated.isAfter(link.lastUpdated())) {
                repository.updateLastChecked(link, newLastUpdated);
                LOGGER.info("Обновлена lastUpdated в БД для ссылки:" + link.url());
                sendUpdateToUsers(link);
            } else {
                LOGGER.info("Нет обновлений для ссылки " + link.url());
            }
        }
    }

    private void sendUpdateToUsers(LinkDto link) {
        List<Long> chatIds = repository.findUsersTrackingLink(link.id());
        botNotifier.sendUpdate(new LinkUpdateDto(0, link.url(), "Новое обновление", chatIds));
    }

    private OffsetDateTime getLastUpdatedTime(String url) {
        if (url.contains("github.com")) {
            return gitHubClient.getLastUpdated(url);
        } else if (url.contains("stackoverflow.com")) {
            return stackOverflowClient.getLastUpdated(url);
        }
        return OffsetDateTime.MIN;
    }
}
