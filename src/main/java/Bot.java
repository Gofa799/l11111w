import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Bot extends TelegramLongPollingBot {

    private final Map<Long, Integer> lastBotMessages = new ConcurrentHashMap<>();

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message userMessage = update.getMessage();
            Long chatId = userMessage.getChatId();
            String messageText = userMessage.getText();

            deleteUserMessage(chatId, userMessage.getMessageId());

            switch (messageText) {
                case "/start" -> sendStartMessage(chatId);
                case "FAQ" -> sendFaq(chatId);
                case "Select Game" -> sendGameSelection(chatId);
                default -> sendText(chatId, "Unknown command. Please use the buttons.");
            }

        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void handleCallback(CallbackQuery query) {
        Long chatId = query.getMessage().getChatId();
        String data = query.getData();

        switch (data) {
            case "FAQ" -> sendFaq(chatId);
            case "BACK_TO_MENU" -> sendStartMessage(chatId);
            case "GAME_1" -> sendText(chatId, "🎲 You selected Game 1.");
            case "GAME_2" -> sendText(chatId, "🎯 You selected Game 2.");
        }
    }

    private void sendStartMessage(Long chatId) {
        String text = """
            👋 Welcome!
            
            Use the buttons below to continue.
            """;

        InlineKeyboardButton faqButton = new InlineKeyboardButton("📖 FAQ");
        faqButton.setCallbackData("FAQ");

        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
        inlineMarkup.setKeyboard(List.of(List.of(faqButton)));

        KeyboardRow row = new KeyboardRow();
        row.add("Select Game");
        row.add("FAQ");

        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        replyKeyboard.setKeyboard(List.of(row));

        Integer messageId = lastBotMessages.get(chatId);
        if (messageId != null && messageId != 0) {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId.toString());
            edit.setMessageId(messageId);
            edit.setText(text);
            edit.setParseMode("HTML");
            edit.setReplyMarkup(inlineMarkup); // Только inline markup в редактировании
            try {
                execute(edit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SendMessage send = new SendMessage();
            send.setChatId(chatId.toString());
            send.setText(text);
            send.setParseMode("HTML");
            send.setReplyMarkup(replyKeyboard); // Только reply markup при первом сообщении
            try {
                Message message = execute(send);
                lastBotMessages.put(chatId, message.getMessageId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFaq(Long chatId) {
        String faqText = """
                📖 <b>FAQ</b>
                
                • This bot is for entertainment and informational use only.
                • The user is fully responsible for their activity.
                • No personal data is stored.
                • Use implies agreement to all terms.
                """;

        EditMessageText edit = new EditMessageText();
        edit.setChatId(chatId.toString());
        edit.setMessageId(lastBotMessages.getOrDefault(chatId, 0));
        edit.setText(faqText);
        edit.setParseMode("HTML");

        InlineKeyboardButton backButton = new InlineKeyboardButton("⬅️ Back");
        backButton.setCallbackData("BACK_TO_MENU");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(backButton)));
        edit.setReplyMarkup(markup);

        try {
            execute(edit);
        } catch (Exception e) {
            sendText(chatId, faqText);
        }
    }

    private void sendGameSelection(Long chatId) {
        String text = "🎮 Choose a game from the options below:";

        InlineKeyboardButton game1 = new InlineKeyboardButton("Game 1 🎲");
        game1.setCallbackData("GAME_1");

        InlineKeyboardButton game2 = new InlineKeyboardButton("Game 2 🎯");
        game2.setCallbackData("GAME_2");

        InlineKeyboardButton back = new InlineKeyboardButton("⬅️ Back");
        back.setCallbackData("BACK_TO_MENU");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(game1, game2), List.of(back)));

        Integer messageId = lastBotMessages.get(chatId);
        if (messageId != null && messageId != 0) {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId.toString());
            edit.setMessageId(messageId);
            edit.setText(text);
            edit.setParseMode("HTML");
            edit.setReplyMarkup(markup);
            try {
                execute(edit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            message.setParseMode("HTML");
            message.setReplyMarkup(markup);
            try {
                Message sent = execute(message);
                lastBotMessages.put(chatId, sent.getMessageId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendText(Long chatId, String text) {
        Integer messageId = lastBotMessages.get(chatId);
        if (messageId != null && messageId != 0) {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId.toString());
            edit.setMessageId(messageId);
            edit.setText(text);
            edit.setParseMode("HTML");
            try {
                execute(edit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SendMessage send = new SendMessage();
            send.setChatId(chatId.toString());
            send.setText(text);
            send.setParseMode("HTML");
            try {
                Message message = execute(send);
                lastBotMessages.put(chatId, message.getMessageId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteUserMessage(Long chatId, Integer messageId) {
        DeleteMessage delete = new DeleteMessage();
        delete.setChatId(chatId.toString());
        delete.setMessageId(messageId);

        try {
            execute(delete);
        } catch (Exception ignored) {
        }
    }
}