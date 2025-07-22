import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

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

            String reply = "Вы написали: " + messageText;

            sendOrEditMessage(chatId, reply);
        }
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
        } catch (Exception e) {

        }
    }
}
