package ru.vshpit.bot;

import org.checkerframework.checker.units.qual.C;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.vshpit.db.Database;
import ru.vshpit.service.CommandService;
import ru.vshpit.service.KeyBoardService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TelegramBot extends TelegramLongPollingBot {
    private static TelegramBot telegramBot;
    private String botUserName;
    private String botToken;
    private static String passwordAdmin;

    public TelegramBot(String botUserName, String botToken, String passwordAdmin) {
        this.botUserName = botUserName;
        this.botToken = botToken;
        this.passwordAdmin = passwordAdmin;
        telegramBot = this;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData() != null) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            System.out.println("Message with callback: " + callbackQuery.getData());
            addNewChatIdInDatabase(callbackQuery.getMessage().getChatId());
            SendMessage sendMessage = getSendMessage(callbackQuery.getData(), update.getCallbackQuery().getMessage().getChatId());
            sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.getMessage() != null && update.getMessage().getText() != null) {
            System.out.println("Message " + update.getMessage().getText());
            addNewChatIdInDatabase(update.getMessage().getChatId());
            SendMessage sendMessage = getSendMessage(update.getMessage());
//            sendMessage.setReplyMarkup(KeyBoardService.getStartMenu());
            sendMessage.setChatId(update.getMessage().getChatId());
//            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            System.out.println(sendMessage);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }

    public static void sendPhoneNumber(String message) {
        Database database = new Database();
        Connection connection = database.getConnection();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from userquiz where is_admin=true");
            while (resultSet.next()) {
                long chatId = resultSet.getLong("chatId");
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText("У вас новое сообщение:\n" + message);
                sendMessage.setChatId(chatId);
                telegramBot.execute(sendMessage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //инициализируем пользователя в бд
    private void addNewChatIdInDatabase(long chatId) {
        Database database = new Database();
        Connection connection = database.getConnection();
        try {
            Statement statement = connection.createStatement();
            String query = "insert into userQuiz (chatId, currentQuizId, nextStepQuizQuestionId)" +
                    " values (" + chatId + ",null,null) on conflict (chatId) do nothing ;";
            statement.execute(query);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private SendMessage getSendMessage(Message message) {
        return getSendMessage(message.getText(), message.getChatId());
    }

    private SendMessage getSendMessage(String message, long chatId) {
        CommandService commandService = new CommandService(chatId);
        return commandService.getMessage(message);
    }

    public static boolean checkAdminPassword(String passwordAdmin) {
        return passwordAdmin.equals(TelegramBot.passwordAdmin);
    }

    public void botConnect() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}
