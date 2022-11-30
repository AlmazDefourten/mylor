package ru.vshpit.service;

import org.checkerframework.checker.units.qual.A;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.vshpit.db.Database;
import ru.vshpit.model.Commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KeyBoardService {
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

    public static InlineKeyboardMarkup getNextQuestion(int quizId,int stepId){
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Далее");
        inlineKeyboardButton.setCallbackData("quizId:"+quizId+"/stepId:"+stepId);
        keyboardButtonsRow.add(inlineKeyboardButton);
        keyboard.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup wantStartTest(){
        InlineKeyboardMarkup inlineKeyboardMarkup=new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard=new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Да");
        inlineKeyboardButton1.setCallbackData(Commands.WANT_QUIZ_START.getCommand());
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Я уже проходил");
        inlineKeyboardButton2.setCallbackData(Commands.PASSED_QUIZ_START.getCommand());

        keyboardButtonsRow.add(inlineKeyboardButton1);
        keyboardButtonsRow.add(inlineKeyboardButton2);

        keyboard.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
}
