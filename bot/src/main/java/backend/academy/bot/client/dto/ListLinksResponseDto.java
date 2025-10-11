package backend.academy.bot.client.dto;

import java.util.List;

public record ListLinksResponseDto
    (int size,
    List<LinkResponseDto> links)
{ }
