package ru.vshpit.service;

import org.checkerframework.checker.units.qual.C;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.vshpit.bot.TelegramBot;
import ru.vshpit.db.Database;
import ru.vshpit.model.Commands;
import ru.vshpit.model.ConstantMessage;
import ru.vshpit.model.SpecialQuiz;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class CommandService {

    private long chatId;

    public CommandService(long chatId) {
        this.chatId = chatId;
    }

    public SendMessage getMessage(String message) {
        String answerMessage = listOfCommands();
        SendMessage sendMessage = new SendMessage();
        if (message.matches(Commands.NEXT_QUESTION_QUIZ.getCommand())) { //получить следующий вопрос
            String parameters[] = message.split("/");
            Map<String, Integer> ids = new HashMap<>();
            for (String parameter : parameters) {
                ids.put(parameter.split(":")[0], Integer.parseInt(parameter.split(":")[1]));
            }
            int quizId = ids.get("quizId");
            int stepId = ids.get("stepId");
            Database database = new Database();
            Connection connection = database.getConnection();
            boolean hasNext = false;
            boolean isCorrectMessage = false;
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSetForCheckStep = statement.executeQuery("select *\n" +
                        "from userquiz where chatid=" + chatId + " and not nextstepquizquestionid is null and currentquizid=" + quizId + ";");
                statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT count(id) FROM quizquestion" +
                        " where step=" + (stepId + 1) + " and quizid=" + quizId);
                if (resultSetForCheckStep.next()) {
                    int checkStepId = resultSetForCheckStep.getInt("nextStepQuizQuestionId");
                    if (checkStepId == stepId) {
                        isCorrectMessage = true;
                    }
                }
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    if (count == 1) {
                        hasNext = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (isCorrectMessage) {
                if (hasNext) {
                    try {
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM quizquestion where step=" + (stepId + 1) + " and quizid=" + quizId);
                        if (resultSet.next()) {
                            sendMessage.setText(resultSet.getString("questionText"));
                            statement.close();
                            statement = connection.createStatement();
                            statement.execute("update userquiz set nextstepquizquestionid=" + (stepId + 1));
                        }
                        sendMessage.setReplyMarkup(KeyBoardService.getNextVariablesOfAnswer(quizId, stepId + 1));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Statement statement = connection.createStatement();
                        statement.execute("update userquiz SET nextstepquizquestionid=null,currentquizid=null where chatid=" + chatId);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        e.printStackTrace();
                    }
                    sendMessage.setText(ConstantMessage.END_DIAGNOSTIC.getMessage());
                    sendMessage.setReplyMarkup(KeyBoardService.lookOtherDiagnosticOrResult(quizId));
                }
            } else {
                sendMessage.setText(ConstantMessage.UNCORRECTED_MESSAGE.getMessage());
            }

        } else if (message.matches(Commands.NEXT_STEP_QUIZ.getCommand())) { //Получить ответ на вопрос
            String parameters[] = message.split("/");
            Map<String, Integer> ids = new HashMap<>();
            for (String parameter : parameters) {
                ids.put(parameter.split(":")[0], Integer.parseInt(parameter.split(":")[1]));
            }
            int quizId = ids.get("quizId");
            int stepId = ids.get("stepId");
            int answerId = ids.get("answerId");
            Database database = new Database();
            Connection connection = database.getConnection();
            try {
                Statement statement = connection.createStatement();
                String query = "SELECT * from quizanswer where id=" + answerId + " and \n" +
                        "quizquestionid=(select id from quizquestion\n" +
                        "where step=" + stepId + " and\n" +
                        "quizid=(select currentquizid from userquiz where chatid=" + chatId + " and (nextstepquizquestionid=" + (stepId) + " or (nextstepquizquestionid =1 and " + (stepId - 1) + "=0))))";
                ResultSet resultSet = statement.executeQuery(query);
                if (resultSet.next()) {
                    String resultText = resultSet.getString("resultText");
                    if (resultText.equals("")) {
                        return getMessage("quizId:" + quizId + "/stepId:" + stepId);
                    }
                    sendMessage.setText(resultText);
                    sendMessage.setReplyMarkup(KeyBoardService.getNextQuestion(quizId, stepId));
                } else {
                    sendMessage.setText(ConstantMessage.UNCORRECTED_MESSAGE.getMessage());
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        /*else if (hasCurrentQuiz()){
            sendMessage.setText("Вы нажали не туда)");
        }*/
        else {
            if (message.equals(Commands.START.getCommand())) { // команда /start
                answerMessage = startMessage();
                sendMessage.setText(answerMessage);
                sendMessage.setReplyMarkup(KeyBoardService.wantStartTest());
            } else if (message.equals(Commands.PASSED_QUIZ_START.getCommand())) { //кнопка уже проходил диагностику
                sendMessage.setText(ConstantMessage.WANT_OTHER_QUIZS.getMessage());
                sendMessage.setReplyMarkup(KeyBoardService.seeOtherQuizs());
                return sendMessage;
            } else if (message.equals(Commands.WANT_QUIZ_START.getCommand())) { //кнопка начать стартовый опрос
                Database database = new Database();
                Connection connection = database.getConnection();
                int stepId = 0;
                try {
                    Statement statement = connection.createStatement();
                    statement.execute("update userquiz\n" +
                            "set currentquizid =" + SpecialQuiz.START_QUIZ.getIdQuiz() + ",nextstepquizquestionid=" + (stepId + 1) + "" +
                            " where chatid=" + chatId + ";");
                    sendMessage.setReplyMarkup(KeyBoardService.getNextVariablesOfAnswer(SpecialQuiz.START_QUIZ.getIdQuiz(),
                            stepId));
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM quizquestion where step=" + (stepId + 1) + " and quizid=" + SpecialQuiz.START_QUIZ.getIdQuiz());
                    if (resultSet.next()) {
                        sendMessage.setText(resultSet.getString("questionText"));
                    }
                    sendMessage.setReplyMarkup(KeyBoardService.getNextVariablesOfAnswer(SpecialQuiz.START_QUIZ.getIdQuiz(),
                            stepId + 1));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (message.equals(Commands.WANT_DIAGNOSTICS.getCommand())) { //хочу пройти дигностику
                sendMessage.setReplyMarkup(KeyBoardService.listDiagnostic());
                sendMessage.setText(ConstantMessage.CHOOSE_DIAGNOSTIC.getMessage());
                return sendMessage;
            } else if (message.matches(Commands.WANT_OTHER_QUIZ.getCommand())) { //кнопка начать опрос по id
                String parameters[] = message.replace("/wantOtherQuiz/", "").split("/");
                Map<String, Integer> ids = new HashMap<>();
                for (String parameter : parameters) {
                    ids.put(parameter.split(":")[0], Integer.parseInt(parameter.split(":")[1]));
                }
                int quizId = ids.get("quizId");
                Database database = new Database();
                Connection connection = database.getConnection();
                int stepId = 0;
                try {
                    Statement statement = connection.createStatement();
                    statement.execute("update userquiz\n" +
                            "set currentquizid =" + quizId + ",nextstepquizquestionid=" + (stepId + 1) + "" +
                            " where chatid=" + chatId + ";");
                    sendMessage.setReplyMarkup(KeyBoardService.getNextVariablesOfAnswer(quizId,
                            stepId));
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM quizquestion where step=" + (stepId + 1) + " and quizid=" + quizId);
                    if (resultSet.next()) {
                        sendMessage.setText(resultSet.getString("questionText"));
                    }
                    sendMessage.setReplyMarkup(KeyBoardService.getNextVariablesOfAnswer(quizId,
                            stepId + 1));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (message.equals(Commands.SEE_OTHER_QUIZ.getCommand())) { //посмотреть список других опросов
                InlineKeyboardMarkup inlineKeyboardMarkup = KeyBoardService.wantTakeOtherTests();
                if (inlineKeyboardMarkup.getKeyboard().size() == 0) {
                    sendMessage.setText(ConstantMessage.DONT_HAS_QUIZS.getMessage());
                } else {
                    sendMessage.setText(ConstantMessage.LIST_OF_QUIZS.getMessage());
                    sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                }
                return sendMessage;
            } else if (message.equals(Commands.LOOK_RESULT.getCommand())) {
                sendMessage.setText("Оставьте ваши контакты и мы свяжемся с вами, также можете указать способ связи (telegram, звонок), укажите всё в одном сообщении");
                Database database = new Database();
                Connection connection = database.getConnection();
                try {
                    Statement statement = connection.createStatement();
                    statement.execute("update userquiz set has_send_email=true where chatid=" + chatId);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (message.equals(Commands.DELETE_ME_ADMIN.getCommand())) {
                Database database = new Database();
                Connection connection = database.getConnection();
                try (Statement statement = connection.createStatement()) {
                    statement.execute("update userquiz set is_admin=false where chatid=" + chatId);
                    connection.close();
                    sendMessage.setText("Вы отписались от рассылки");
                    return sendMessage;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (message.matches(Commands.SET_ME_ADMIN.getCommand())) {
                String password = message.substring(message.lastIndexOf(":") + 1);
                if (TelegramBot.checkAdminPassword(password)) {
                    Database database = new Database();
                    Connection connection = database.getConnection();
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("update userquiz set is_admin=true where chatid=" + chatId);
                        connection.close();
                        sendMessage.setText("Вы подписались на рассылку!");
                        return sendMessage;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else { //иначе получить ответ на вопрос(зависит от состояния в бд) либо отправить answerMessage(инициализируется в начале, либо в условиях)
                Database database = new Database();
                Connection connection = database.getConnection();
                boolean hasMessage = false;
                try {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("select * from userquiz where chatid=" + chatId);
                    if (resultSet.next()) {
                        hasMessage = resultSet.getBoolean("has_send_email");
                        statement = connection.createStatement();
                        statement.execute("update userquiz set has_send_email=false where chatid=" + chatId);
                        statement.close();
                    }
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (hasMessage) {
                    //todo сделать приём номера телефона и отправка по email
                    sendMessage.setText("Сообщение успешно отправлено: " + message);
                    TelegramBot.sendPhoneNumber(message);
                    return sendMessage;
                } else {
                    sendMessage.setText(answerMessage);
                }
            }
        }


        return sendMessage;
    }

    private String startMessage() {
        return ConstantMessage.GREETING.getMessage();
    }

    private String listOfCommands() {
        return ConstantMessage.TEXT_FOR_START_BUTTON.getMessage();
    }

    private boolean hasCurrentQuiz() {
        Database database = new Database();
        Connection connection = database.getConnection();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select count(chatid)\n" +
                    "from userquiz where chatid=" + chatId + " and nextstepquizquestionid is null and currentquizid is null;");
            if (resultSet.next()) {
                if (resultSet.getInt("count") == 1) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
