import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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
                case "FAQ" -> sendOrEditMessage(chatId, getFaqText());
                case "Select Game" -> sendOrEditMessage(chatId, "🎮 Choose a game from the available options.");
                default -> sendOrEditMessage(chatId, "You wrote: " + messageText);
            }
        }
    }

    private void sendStartMessage(Long chatId) {
        String text = """
                👋 Welcome!
                
                Use the buttons below to continue.
                """;

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");

        // Inline button
        InlineKeyboardButton faqButton = new InlineKeyboardButton("📖 FAQ");
        faqButton.setText("📖 FAQ");
        faqButton.setCallbackData("FAQ");

        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
        inlineMarkup.setKeyboard(List.of(List.of(faqButton)));

        // Reply keyboard
        KeyboardRow row = new KeyboardRow();
        row.add("Select Game");
        row.add("FAQ");

        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setResizeKeyboard(true);
        replyKeyboard.setKeyboard(List.of(row));

        sendMessage.setReplyMarkup(replyKeyboard);

        try {
            Message message = execute(sendMessage);
            lastBotMessages.put(chatId, message.getMessageId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFaqText() {
        return """
                📖 <b>FAQ</b>
                
                • This bot is for entertainment and information purposes only.
                • The user is solely responsible for their actions.
                • We do not store personal data or guarantee any outcomes.
                • Usage implies acceptance of all terms.
                """;
    }

    private void sendOrEditMessage(Long chatId, String newText) {
        Integer lastMessageId = lastBotMessages.get(chatId);

        if (lastMessageId != null) {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId.toString());
            edit.setMessageId(lastMessageId);
            edit.setText(newText);
            edit.setParseMode("HTML");

            try {
                execute(edit);
            } catch (Exception e) {
                Message newMessage = sendNewMessage(chatId, newText);
                if (newMessage != null) {
                    lastBotMessages.put(chatId, newMessage.getMessageId());
                }
            }
        } else {
            Message newMessage = sendNewMessage(chatId, newText);
            if (newMessage != null) {
                lastBotMessages.put(chatId, newMessage.getMessageId());
            }
        }
    }

    private Message sendNewMessage(Long chatId, String text) {
        SendMessage send = new SendMessage();
        send.setChatId(chatId.toString());
        send.setText(text);
        send.setParseMode("HTML");

        try {
            return execute(send);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
