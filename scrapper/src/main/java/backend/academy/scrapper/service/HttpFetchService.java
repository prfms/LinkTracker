package backend.academy.scrapper.service;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.logging.Logger;

@Service
public class HttpFetchService {
    private final WebClient webClient;
    private static final Logger LOGGER = Logger.getLogger(HttpFetchService.class.getName());

    public HttpFetchService(WebClient genericWebClient) {
        this.webClient = genericWebClient;
    }

    public Mono<FetchedPage> fetch(String url, String etag, String lastModified) {

        LOGGER.info(() -> "HTTP FETCH → " + url +
            " | etag=" + etag +
            " | lastModified=" + lastModified);

        return webClient.get()
            .uri(url)
            .headers(h -> {
                if (etag != null && !etag.isBlank()) {
                    h.setIfNoneMatch(etag);
                    LOGGER.fine(() -> " → Added If-None-Match: " + etag);
                }
                if (lastModified != null && !lastModified.isBlank()) {
                    h.set(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
                    LOGGER.fine(() -> " → Added If-Modified-Since: " + lastModified);
                }
            })
            .retrieve()
            .toEntity(byte[].class)
            .timeout(Duration.ofSeconds(10))
            .onErrorResume(ex -> {
                LOGGER.warning("HTTP FETCH ERROR (" + url + "): " + ex.getMessage());
                return Mono.just(ResponseEntity.status(599).build());
            })
            .map(resp -> {
                int sc = resp.getStatusCode().value();

                LOGGER.info(() ->
                    "HTTP RESPONSE ← " + url +
                        " | status=" + sc +
                        " | etag=" + resp.getHeaders().getETag() +
                        " | lastModified=" + resp.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED) +
                        " | contentType=" + resp.getHeaders().getContentType()
                );

                if (sc == 304) {
                    LOGGER.info(" → NOT MODIFIED (ETag match)");
                    return FetchedPage.notModified();
                }

                if (sc >= 200 && sc < 300) {
                    byte[] body = resp.getBody();
                    MediaType ct = resp.getHeaders().getContentType();
                    return FetchedPage.modified(
                        resp.getHeaders().getETag(),
                        resp.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED),
                        body, ct
                    );
                }

                LOGGER.warning(" → ERROR RESPONSE: " + sc);
                return FetchedPage.error(sc);
            });
    }

    public static final class FetchedPage {
        public final String etag;
        public final String lastModified;
        public final byte[] body;
        public final MediaType contentType;
        public final boolean notModified;
        public final Integer statusCode;

        private FetchedPage(String etag, String lastModified, byte[] body, MediaType contentType, boolean notModified, Integer statusCode) {
            this.etag = etag;
            this.lastModified = lastModified;
            this.body = body;
            this.contentType = contentType;
            this.notModified = notModified;
            this.statusCode = statusCode;
        }

        public static FetchedPage notModified() {
            return new FetchedPage(null, null, null, null, true, 304);
        }

        public static FetchedPage modified(String etag, String lastModified, byte[] body, MediaType contentType) {
            return new FetchedPage(etag, lastModified, body, contentType, false, 200);
        }

        public static FetchedPage error(int status) {
            return new FetchedPage(null, null, null, null, false, status);
        }
    }
}
