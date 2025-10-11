package backend.academy.scrapper.controller.dto;

import java.util.List;

public record LinkUpdateDto
    (int id,
     String url,
     String description,
     List<Long> tgChatIds)
{ }
