package backend.academy.bot.client.dto;

import java.util.List;

public record LinkResponseDto(
    int id,
    String url,
    List<String> tags,
    List<String> filters)
    { }
