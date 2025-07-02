package backend.academy.bot.service;

import backend.academy.bot.BotConfig;
import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.dto.AddLinkRequestDto;
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
import java.util.List;
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
                new BotCommand("/help", "Получить помощь по командам")));
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null) {
                    handleMessage(update.message());
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void sendUpdate(Long chatId, String url) {
        bot.execute(new SendMessage(chatId, "Обнаружены изменения по ссылке " + url));
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
                                    .map(link -> link.url() + " (Теги: " + String.join(", ", link.tags()) + ")")
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
                    StringBuilder helpMessage = new StringBuilder("Доступные команды:\n");
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
            default:
                sendMessage(chatId, "Неизвестная команда. Используйте /help для списка команд.");
        }
    }

    private void processUserState(long chatId, String text) {
        BotState state = userStates.get(chatId);

        switch (state.step) {
            case AWAITING_LINK_TRACK:
                if (isValidLink(text)) {
                    state.link = text;
                    state.step = BotStep.AWAITING_TAGS;
                    sendMessage(chatId, "Введите теги через пробел (или пропустите, отправив /skip):");
                } else {
                    sendMessage(chatId, "Некорректный формат ссылки. Поддерживаются ссылки с GitHub и StackOverFlow.");
                }
                break;

            case AWAITING_TAGS:
                if (!text.equals("/skip")) {
                    state.tags = List.of(text.split(" "));
                }
                state.step = BotStep.AWAITING_FILTERS;
                sendMessage(chatId, "Введите фильтры через пробел (или пропустите, отправив /skip):");
                break;

            case AWAITING_FILTERS:
                if (!text.equals("/skip")) {
                    state.filters = List.of(text.split(" "));
                }
                scrapperClient.addLink(chatId, new AddLinkRequestDto(state.link, state.tags, state.filters));
                sendMessage(chatId, "Ссылка успешно добавлена!");
                userStates.remove(chatId);
                break;

            case AWAITING_LINK_UNTRACK:
                state.link = text;
                scrapperClient.deleteLink(chatId, new RemoveLinkRequestDto(state.link));
                sendMessage(chatId, "Ссылка успешно удалена из отслеживания!");
                userStates.remove(chatId);
                break;
        }
    }

    private boolean isValidLink(String link) {
        return Pattern.matches("^https:\\/\\/stackoverflow\\.com\\/questions\\/\\d+\\/?", link)
            || Pattern.matches("https:\\/\\/github\\.com\\/[(a-zA-Z0-9_]+\\/[(a-zA-Z0-9_-]+\\/?", link);
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
        private List<String> tags = List.of();
        private List<String> filters = List.of();

        public BotState(BotStep step) {
            this.step = step;
        }
    }

    public enum BotStep {
        AWAITING_LINK_TRACK,
        AWAITING_TAGS,
        AWAITING_FILTERS,
        AWAITING_LINK_UNTRACK,
    }
}
