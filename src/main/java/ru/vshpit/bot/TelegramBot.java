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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TelegramBot extends TelegramLongPollingBot {

    private String botUserName;
    private String botToken;

    public TelegramBot(String botUserName, String botToken) {
        this.botUserName = botUserName;
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    /*@Override
    public void onUpdateReceived(Update update) {
        String message=getMessage(update.getMessage());
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(message);
        System.out.println(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }*/
    @Override
    public void onUpdateReceived(Update update) {
    if(update.hasCallbackQuery() && update.getCallbackQuery().getData()!=null){
            CallbackQuery callbackQuery=update.getCallbackQuery();
            System.out.println("Message with callback"+callbackQuery.getData());
            SendMessage sendMessage=getSendMessage(callbackQuery.getData(),update.getCallbackQuery().getMessage().getChatId());
            sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }else if(update.getMessage()!=null && update.getMessage().getText()!=null){
            System.out.println("Message "+update.getMessage().getText());
            addNewChatIdInDatabase(update.getMessage().getChatId());
            SendMessage sendMessage=getSendMessage(update.getMessage(),update.getMessage().getChatId());
//            sendMessage.setReplyMarkup(KeyBoardService.getStartMenu());
            sendMessage.setChatId(update.getMessage().getChatId());
//            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            System.out.println(sendMessage);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }

    }

    private void addNewChatIdInDatabase(long chatId){
        Database database=new Database();
        Connection connection=database.getConnection();
        try {
            Statement statement=connection.createStatement();
            String query="insert into userQuiz (chatId, currentQuizId, nextStepQuizQuestionId)" +
                    " values ("+chatId+",null,null) on conflict (chatId) do nothing ;";
            statement.execute(query);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private SendMessage getSendMessage(Message message, long chatId){
        CommandService commandService=new CommandService(chatId);
        return commandService.getMessage(message.getText());
    }
    private SendMessage getSendMessage(String message, long chatId){
        CommandService commandService=new CommandService(chatId);
        return commandService.getMessage(message);
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
