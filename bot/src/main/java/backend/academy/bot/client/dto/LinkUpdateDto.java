package backend.academy.bot.client.dto;

import java.util.List;

public record LinkUpdateDto(int id, String url, String description, List<Long> tgChatIds) {}
