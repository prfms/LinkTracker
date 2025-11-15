package backend.academy.scrapper.controller.dto;

import java.time.OffsetDateTime;

public record UpdateInfo (OffsetDateTime lastUpdatedAt, String description) {
}
