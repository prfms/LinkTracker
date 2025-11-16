package backend.academy.bot.service;

import backend.academy.bot.BotConfig;
import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.dto.AddLinkRequestDto;
import backend.academy.bot.client.dto.LinkResponseDto;
import backend.academy.bot.client.dto.ListLinksResponseDto;
import backend.academy.bot.client.dto.RemoveLinkRequestDto;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetMyCommands;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotService {
    private final TelegramBot bot;
    private final ScrapperClient scrapperClient;

    @Getter
    private final Map<Long, BotState> userStates = new HashMap<>();

    @Autowired
    public BotService(BotConfig botConfig, ScrapperClient scrapperClient) {
        this.bot = new TelegramBot(botConfig.telegramToken());
        this.scrapperClient = scrapperClient;
    }

    @PostConstruct
    public void init() {
        bot.execute(new SetMyCommands(
                new BotCommand("/start", "Запуск бота"),
                new BotCommand("/track", "Добавить ссылку для отслеживания"),
                new BotCommand("/untrack", "Удалить ссылку из отслеживания"),
                new BotCommand("/list", "Показать отслеживаемые ссылки"),
                new BotCommand("/help", "Получить помощь по командам"),
                new BotCommand("/exit", "Отменить действие, выйти")));
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null) {
                    handleMessage(update.message());
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void sendUpdate(Long chatId, String url, String description) {
        bot.execute(new SendMessage(chatId, description + "\n" + url));
    }

    public void handleMessage(Message message) {
        long chatId = message.chat().id();
        String text = message.text();

        if (userStates.containsKey(chatId)) {
            processUserState(chatId, text);
            return;
        }

        switch (text) {
            case "/start":
                scrapperClient.registerUser(chatId);
                sendMessage(chatId, "Привет! Это бот для отслеживания веб-ресурсов.");
                break;
            case "/track":
                userStates.put(chatId, new BotState(BotStep.AWAITING_LINK_TRACK));
                sendMessage(chatId, "Введите ссылку, которую хотите отслеживать:");
                break;
            case "/list":
                ListLinksResponseDto listLinks = scrapperClient.getLinks(chatId);
                if (listLinks.size() == 0) {
                    sendMessage(chatId, "У вас нет отслеживаемых ссылок.");
                } else {
                    String response = "Отслеживаемые ссылки:\n"
                            + listLinks.links().stream()
                                    .map(LinkResponseDto::url)
                                    .reduce("", (a, b) -> a + "\n" + b);
                    sendMessage(chatId, response);
                }
                break;
            case "/untrack":
                userStates.put(chatId, new BotState(BotStep.AWAITING_LINK_UNTRACK));
                sendMessage(chatId, "Введите ссылку, которую хотите удалить из отслеживания:");
                break;
            case "/help":
                GetMyCommands getMyCommands = new GetMyCommands();
                BotCommand[] commands = bot.execute(getMyCommands).commands();

                if (commands != null && commands.length > 0) {
                    StringBuilder helpMessage = new StringBuilder(
                            "Бот отслеживает изменения содержимого и уведомляет вас при обновлениях.\nДоступные команды:\n");
                    for (BotCommand command : commands) {
                        helpMessage
                                .append("/")
                                .append(command.command())
                                .append(" - ")
                                .append(command.description())
                                .append("\n");
                    }
                    sendMessage(chatId, helpMessage.toString());
                } else {
                    sendMessage(chatId, "Список команд не доступен.");
                }
                break;
            case "/exit":
                userStates.remove(chatId);
                sendMessage(chatId, "Действие отменено.");
                return;
            default:
                sendMessage(chatId, "Неизвестная команда. Используйте /help для списка команд.");
        }
    }

    private void processUserState(long chatId, String text) {
        if ("/exit".equalsIgnoreCase(text)) {
            userStates.remove(chatId);
            sendMessage(chatId, "Действие отменено. Вы можете использовать другие команды.");
            return;
        }

        BotState state = userStates.get(chatId);
        if (state == null) return;

        switch (state.step) {
            case AWAITING_LINK_TRACK:
                if (text == null || text.isBlank()) {
                    sendMessage(chatId, "Пустая строка. Введите ссылку или /exit для отмены.");
                    return;
                }
                if (!isValidLink(text)) {
                    sendMessage(
                            chatId,
                            """
                                Некорректный формат ссылки.
                                Допустимые форматы для отслеживания:

                                • GitHub:
                                    - https://github.com/user/repo
                                    - https://github.com/user/repo/issues/123
                                    - https://github.com/user/repo/pull/45

                                • StackOverflow:
                                    - https://stackoverflow.com/questions/12345

                                • Обычные веб-страницы (HTML):
                                    - Любая ссылка вида https://example.com

                                Попробуйте ещё раз или введите /exit для выхода.
                                """);
                    return;
                }
                try {
                    ListLinksResponseDto existingLinks = scrapperClient.getLinks(chatId);
                    boolean alreadyExists = existingLinks.links().stream()
                            .anyMatch(link -> link.url().equals(text));
                    if (alreadyExists) {
                        sendMessage(chatId, "Эта ссылка уже отслеживается.");
                        return;
                    }

                    state.link = text;
                    scrapperClient.addLink(chatId, new AddLinkRequestDto(state.link));
                    userStates.remove(chatId);
                    sendMessage(chatId, "Ссылка успешно добавлена!");
                } catch (Exception e) {
                    sendMessage(chatId, "Ошибка при добавлении ссылки. Попробуйте позже.");
                }
                break;

            case AWAITING_LINK_UNTRACK:
                if (text == null || text.isBlank()) {
                    sendMessage(chatId, "Пустая строка. Введите ссылку или /exit для отмены.");
                    return;
                }
                try {
                    ListLinksResponseDto existingLinks = scrapperClient.getLinks(chatId);
                    boolean exists = existingLinks.links().stream()
                            .anyMatch(link -> link.url().equals(text));
                    if (!exists) {
                        sendMessage(chatId, "Эта ссылка не найдена в ваших отслеживаемых.");
                        return;
                    }

                    state.link = text;
                    scrapperClient.deleteLink(chatId, new RemoveLinkRequestDto(state.link));
                    userStates.remove(chatId);
                    sendMessage(chatId, "Ссылка успешно удалена из отслеживания!");
                } catch (Exception e) {
                    sendMessage(chatId, "Ошибка при удалении ссылки. Попробуйте позже.");
                }
                break;
        }
    }

    private boolean isValidLink(String link) {
        return Pattern.matches("^https:\\/\\/stackoverflow\\.com\\/questions\\/\\d+\\/?", link)
                || Pattern.matches("https:\\/\\/github\\.com\\/[(a-zA-Z0-9_]+\\/[(a-zA-Z0-9_-]+\\/?", link)
                || Pattern.matches("^https:\\/\\/[\\w.-]+(\\/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]*)?$", link);
    }

    private void sendMessage(long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }

    @Getter
    public static class BotState {
        @Getter
        private BotStep step;

        @Getter
        private String link;

        public BotState(BotStep step) {
            this.step = step;
        }
    }

    public enum BotStep {
        AWAITING_LINK_TRACK,
        AWAITING_LINK_UNTRACK,
    }
}
