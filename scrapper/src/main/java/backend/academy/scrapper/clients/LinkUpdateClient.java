package backend.academy.scrapper.clients;

import java.time.OffsetDateTime;

public interface LinkUpdateClient {
    OffsetDateTime getLastUpdated(String url);
}
