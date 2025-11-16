package backend.academy.scrapper.service;

import backend.academy.scrapper.clients.GenericHtmlClient;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.controller.dto.AddLinkRequestDto;
import backend.academy.scrapper.controller.dto.LinkResponseDto;
import backend.academy.scrapper.controller.dto.LinkUpdateDto;
import backend.academy.scrapper.controller.dto.RemoveLinkRequestDto;
import backend.academy.scrapper.controller.dto.UpdateInfo;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.model.LinkDto;
import backend.academy.scrapper.repository.CommonRepository;
import backend.academy.scrapper.scheduler.BotNotifier;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScrapperService {
    private final CommonRepository repository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final BotNotifier botNotifier;
    private final GenericHtmlClient htmlClient;
    private static final Logger LOGGER = Logger.getLogger(ScrapperService.class.getName());

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
                    .map(link -> new LinkResponseDto(link.id(), link.url()))
                    .toList();
        } else {
            throw new NotFoundException("Пользователь с ID " + chatId + " не найден.");
        }
    }

    public int addLink(long chatId, AddLinkRequestDto link) {
        return repository.addLink(link.link(), chatId, OffsetDateTime.now(ZoneOffset.UTC));
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
            String url = link.url().toLowerCase();
            UpdateInfo updateInfo = getUpdateInfo(url);
            if (updateInfo == null) {
                LOGGER.info("Обновлений по ссылке " + url + " нет. Пропускаем");
                continue;
            }
            OffsetDateTime newLastUpdated = updateInfo.lastUpdatedAt();
            LOGGER.info("Ссылка: " + url + " | Старый lastUpdated: " + link.lastUpdated() + " | Новый lastUpdated: "
                    + newLastUpdated);

            if (newLastUpdated != null && newLastUpdated.isAfter(link.lastUpdated())) {
                repository.updateLastChecked(link, newLastUpdated);
                LOGGER.info("Обновлена lastUpdated в БД для ссылки:" + url);
                sendUpdateToUsers(link, updateInfo.description());
            } else {
                LOGGER.info("Нет обновлений для ссылки " + url);
            }
        }
    }

    private void sendUpdateToUsers(LinkDto link, String description) {
        List<Long> chatIds = repository.findUsersTrackingLink(link.id());
        botNotifier.sendUpdate(new LinkUpdateDto(0, link.url(), description, chatIds));
    }

    private UpdateInfo getUpdateInfo(String url) {
        if (url.contains("github.com")) {
            return gitHubClient.getUpdateInfo(url);
        } else if (url.contains("stackoverflow.com")) {
            return stackOverflowClient.getUpdateInfo(url);
        } else {
            return htmlClient.getUpdateInfo(url);
        }
    }
}
