package backend.academy.scrapper.clients;

import backend.academy.scrapper.controller.dto.UpdateInfo;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.LinkSignature;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.LinkSignatureRepository;
import backend.academy.scrapper.service.GenericHtmlExtractor;
import backend.academy.scrapper.service.HttpFetchService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenericHtmlClient implements LinkUpdateClient {

    private static final Logger LOGGER = Logger.getLogger(GenericHtmlClient.class.getName());

    private final HttpFetchService fetchService;
    private final GenericHtmlExtractor extractor;
    private final LinkSignatureRepository signatures;
    private final LinkRepository linkRepository;

    @Override
    public UpdateInfo getUpdateInfo(String url) {
        try {
            Optional<Link> linkOpt = linkRepository.findByUrl(url);
            if (linkOpt.isEmpty()) {
                LOGGER.warning(() -> "Ссылка не найдена в таблице links: " + url);
                return null;
            }

            int linkId = linkOpt.get().id();

            LinkSignature prev = signatures.findByLinkId(linkId).orElse(null);
            String etag = prev != null ? prev.etag() : null;
            String lastModified = prev != null ? prev.lastModified() : null;

            var fetched = fetchService.fetchSync(url, etag, lastModified);
            if (fetched == null) {
                LOGGER.warning(() -> "Не удалось получить контент: " + url);
                return null;
            }

            if (fetched.notModified) {
                LOGGER.info(() -> "304 Not Modified → " + url);
                return new UpdateInfo(
                        prev != null ? prev.updatedAt() : OffsetDateTime.now(ZoneOffset.UTC), "Изменений не обнаружено");
            }

            if (fetched.statusCode != null && fetched.statusCode >= 400) {
                LOGGER.warning(() -> "Ошибка загрузки (" + fetched.statusCode + ") → " + url);
                return null;
            }

            if (fetched.contentType != null && !MediaType.TEXT_HTML.isCompatibleWith(fetched.contentType)) {
                LOGGER.warning(() -> "Контент не HTML, пропуск → " + url);
                return null;
            }

            var extracted = extractor.extract(fetched.body, url);
            boolean firstTime = (prev == null);
            boolean changed = prev == null || !Objects.equals(prev.hash(), extracted.compositeHash());

            if (!changed) {
                LOGGER.info(() -> "Хэши совпадают, изменений нет → " + url);
                return null;
            }

            LOGGER.info(() -> (firstTime ? "Первая проверка" : "Обнаружено изменение") + " → " + url);

            LinkSignature sig;
            if (prev == null) {
                sig = LinkSignature.builder()
                        .etag(fetched.etag)
                        .lastModified(fetched.lastModified)
                        .hash(extracted.compositeHash())
                        .updatedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build();
            } else {
                prev.etag(fetched.etag);
                prev.lastModified(fetched.lastModified);
                prev.hash(extracted.compositeHash());
                prev.updatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                sig = prev;
            }

            signatures.save(sig);

            return firstTime ? null : new UpdateInfo(sig.updatedAt(), "Получено обновление по ссылке:");

        } catch (Exception e) {
            LOGGER.warning(() -> "Ошибка проверки HTML ссылки: " + url + " → " + e.getMessage());
            return null;
        }
    }
}
