package backend.academy.scrapper.clients;

import backend.academy.scrapper.controller.dto.UpdateInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class StackOverflowClient implements LinkUpdateClient {
    private final WebClient webClient;

    public StackOverflowClient(WebClient.Builder webClientBuilder) {
        this.webClient =
                webClientBuilder.baseUrl("https://api.stackexchange.com/2.3").build();
    }

    @Override
    public UpdateInfo getUpdateInfo(String questionUrl) {
        String questionId = extractQuestionId(questionUrl);

        AnswersResponse response = webClient
                .get()
                .uri("/questions/{ids}/answers?order=desc&sort=activity&site=stackoverflow", questionId)
                .retrieve()
                .bodyToMono(AnswersResponse.class)
                .block();

        if (response == null || response.items == null || response.items.isEmpty()) {
            return new UpdateInfo(getQuestionLastActivity(questionId), "Новое обновление по вопросу " + questionId);
        }

        long maxTimestamp = response.items.stream()
                .mapToLong(answer -> answer.lastActivityDate)
                .max()
                .orElseThrow(() -> new IllegalStateException("Нет данных об ответах"));

        var time = Instant.ofEpochSecond(maxTimestamp).atOffset(ZoneOffset.UTC);
        return new UpdateInfo(time, "Новое обновление по вопросу " + questionId);
    }

    private OffsetDateTime getQuestionLastActivity(String questionId) {
        QuestionResponse response = webClient
                .get()
                .uri("/questions/{ids}?order=desc&sort=activity&site=stackoverflow", questionId)
                .retrieve()
                .bodyToMono(QuestionResponse.class)
                .block();

        if (response == null || response.items == null || response.items.isEmpty()) {
            throw new IllegalStateException("Вопрос не найден: " + questionId);
        }

        return Instant.ofEpochSecond(response.items.getFirst().lastActivityDate).atOffset(ZoneOffset.UTC);
    }

    private String extractQuestionId(String url) {
        String[] parts = url.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("questions".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        throw new IllegalArgumentException("Невозможно извлечь ID вопроса из URL: " + url);
    }

    private static class AnswersResponse {
        public List<Answer> items;
    }

    private static class Answer {
        @JsonProperty("last_activity_date")
        public long lastActivityDate;
    }

    private static class QuestionResponse {
        public List<Question> items;
    }

    private static class Question {
        @JsonProperty("last_activity_date")
        public long lastActivityDate;
    }
}
