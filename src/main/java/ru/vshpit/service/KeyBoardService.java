package ru.vshpit.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.vshpit.db.Database;
import ru.vshpit.model.Commands;
import ru.vshpit.model.ConstantMessage;
import ru.vshpit.model.SpecialQuiz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KeyBoardService {
    //получить кнопку start
    public static ReplyKeyboardMarkup getStartMenu() {
        // Создаем клавиуатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(false);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();
        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        keyboardFirstRow.add(Commands.START.getCommand());

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        // добавляем список клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    //получить варианты ответов на вопрос
    public static InlineKeyboardMarkup getNextVariablesOfAnswer(int quizId, int stepId) throws SQLException {
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();
        Database database=new Database();
        Connection connection=database.getConnection();
        Statement statement=connection.createStatement();
        String query="select * from quizanswer where quizquestionid=" +
                "(SELECT id FROM quizquestion where step="+stepId+" and quizid="+quizId+") order by id";
        ResultSet resultSet=statement.executeQuery(query);
        while ((resultSet.next())){
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(resultSet.getString("answerText"));
            inlineKeyboardButton.setCallbackData("quizId:"+quizId+"/stepId:"+stepId+"/answerId:"+resultSet.getInt("id"));
            System.out.println("quizId:"+quizId+"/stepId:"+stepId+"/answerId:"+resultSet.getInt("id"));
            keyboardButtonsRow.add(inlineKeyboardButton);
            keyboard.add(keyboardButtonsRow);
        }
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    //получить следующий вопрос
    public static InlineKeyboardMarkup getNextQuestion(int quizId,int stepId){
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(ConstantMessage.NEXT.getMessage());
        inlineKeyboardButton.setCallbackData("quizId:"+quizId+"/stepId:"+stepId);
        keyboardButtonsRow.add(inlineKeyboardButton);
        keyboard.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup lookResult(){
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(ConstantMessage.LOOK_RESULT.getMessage());
        inlineKeyboardButton.setCallbackData(Commands.LOOK_RESULT.getCommand());
        keyboardButtonsRow.add(inlineKeyboardButton);
        keyboard.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    //Пройти опрос или уже проходил(кнопки)
    public static InlineKeyboardMarkup wantStartTest(){
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText(ConstantMessage.WANT_DIAGNOSTIC.getMessage());
        inlineKeyboardButton1.setCallbackData(Commands.WANT_DIAGNOSTICS.getCommand());
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText(ConstantMessage.I_RUN_DIAGNOSTIC.getMessage());
        inlineKeyboardButton2.setCallbackData(Commands.LOOK_RESULT.getCommand());

        keyboardButtonsRow1.add(inlineKeyboardButton1);
        keyboardButtonsRow2.add(inlineKeyboardButton2);

        keyboard.add(keyboardButtonsRow1);
        keyboard.add(keyboardButtonsRow2);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    //кнопка посмотреть список других опросов
    public static InlineKeyboardMarkup seeOtherQuizs(){
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Пройти другие опросы");
        inlineKeyboardButton.setCallbackData(Commands.SEE_OTHER_QUIZ.getCommand());

        keyboardButtonsRow.add(inlineKeyboardButton);
        keyboard.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }



    //Список других опросов(по нажатию на них старт опроса)
    public static InlineKeyboardMarkup wantTakeOtherTests(){
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();

        Database database=new Database();
        Connection connection=database.getConnection();
        try {
            Statement statement=connection.createStatement();
            ResultSet resultSet=statement.executeQuery("SELECT * from quiz where id!="+ SpecialQuiz.START_QUIZ.getIdQuiz());
            while (resultSet.next()){
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(resultSet.getString("title"));
                inlineKeyboardButton.setCallbackData("/wantOtherQuiz/quizId:"+resultSet.getInt("id"));
                keyboardButtonsRow.add(inlineKeyboardButton);
                keyboard.add(keyboardButtonsRow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
    //посмотреть другие опросы, которые не содержат id
    public static InlineKeyboardMarkup lookOtherDiagnosticOrResult(int id){
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();

        Database database=new Database();
        Connection connection=database.getConnection();
        try {
            Statement statement=connection.createStatement();
            ResultSet resultSet=statement.executeQuery("SELECT * from quiz where id!="+ id);
            while (resultSet.next()){
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText("Пройти диагностику: '"+resultSet.getString("title")+"'");
                inlineKeyboardButton.setCallbackData("/wantOtherQuiz/quizId:"+resultSet.getInt("id"));
                keyboardButtonsRow.add(inlineKeyboardButton);
                keyboard.add(keyboardButtonsRow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText(ConstantMessage.LOOK_RESULT.getMessage());
        inlineKeyboardButton.setCallbackData(Commands.LOOK_RESULT.getCommand());
        keyboardButtonsRow.add(inlineKeyboardButton);
        keyboard.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    //Список диагностик(по нажатию на них старт опроса)
    public static InlineKeyboardMarkup listDiagnostic() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        Database database = new Database();
        Connection connection = database.getConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * from quiz");
            while (resultSet.next()) {
                List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(resultSet.getString("title"));
                inlineKeyboardButton.setCallbackData("/wantOtherQuiz/quizId:" + resultSet.getInt("id"));
                keyboardButtonsRow.add(inlineKeyboardButton);
                keyboard.add(keyboardButtonsRow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
}
