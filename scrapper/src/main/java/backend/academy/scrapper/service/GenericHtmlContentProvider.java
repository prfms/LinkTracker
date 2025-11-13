package backend.academy.scrapper.service;

import backend.academy.scrapper.controller.dto.LinkUpdateDto;
import backend.academy.scrapper.model.LinkDto;
import backend.academy.scrapper.model.LinkSignature;
import backend.academy.scrapper.repository.LinkSignatureRepository;
import backend.academy.scrapper.scheduler.BotNotifier;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GenericHtmlContentProvider {

    private static final Logger LOGGER = Logger.getLogger(GenericHtmlContentProvider.class.getName());

    private final HttpFetchService fetchService;
    private final GenericHtmlExtractor extractor;
    private final LinkSignatureRepository signatures;
    private final BotNotifier botNotifier;

    public Mono<Void> checkAndNotify(LinkDto link) {

        LOGGER.info(() -> "ПРОВЕРКА ССЫЛКИ → " + link.url());

        LinkSignature prev = signatures.findByLinkId((long) link.id()).orElse(null);

        String etag = prev != null ? prev.etag() : null;
        String lastModified = prev != null ? prev.lastModified() : null;

        return fetchService.fetch(link.url(), etag, lastModified)
            .flatMap(fp -> {

                if (fp.notModified) {
                    LOGGER.info(() -> "304 Not Modified → " + link.url());
                    return Mono.empty();
                }

                if (fp.statusCode != null && fp.statusCode >= 400) {
                    LOGGER.warning(() -> "Ошибка загрузки (" + fp.statusCode + ") → " + link.url());
                    return Mono.empty();
                }

                if (fp.contentType != null &&
                    !MediaType.TEXT_HTML.isCompatibleWith(fp.contentType)) {
                    LOGGER.warning(() -> "Контент не HTML, пропуск → " + link.url());
                    return Mono.empty();
                }

                var extracted = extractor.extract(fp.body, link.url());

                boolean firstTime = (prev == null);
                boolean changed = prev == null
                    || !Objects.equals(prev.compositeHash(), extracted.compositeHash());

                if (!changed) {
                    LOGGER.info(() -> "Хэши совпадают, изменений нет → " + link.url());
                    return Mono.empty();
                }

                LOGGER.info(() -> (firstTime ? "Первая проверка" : "Обнаружено изменение")
                    + " → " + link.url());

                LinkSignature sig;

                if (prev == null) {
                    sig = LinkSignature.builder()
                        .linkId((long) link.id())
                        .etag(fp.etag)
                        .lastModified(fp.lastModified)
                        .compositeHash(extracted.compositeHash())
                        .updatedAt(OffsetDateTime.now())
                        .build();

                    LOGGER.info("Создана новая сигнатура.");
                } else {
                    prev.etag(fp.etag);
                    prev.lastModified(fp.lastModified);
                    prev.compositeHash(extracted.compositeHash());
                    prev.updatedAt(OffsetDateTime.now());
                    sig = prev;

                    LOGGER.info("Обновлена существующая сигнатура.");
                }

                signatures.save(sig);

                if (!firstTime) {
                    botNotifier.sendUpdate(
                        new LinkUpdateDto(
                            0,
                            link.url(),
                            "Страница обновилась (generic HTML).",
                            List.of(link.chatId())
                        )
                    );
                }

                return Mono.empty();
            });
    }
}
