package ru.vshpit.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.vshpit.db.Database;
import ru.vshpit.model.Commands;
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
        if (message.matches(Commands.NEXT_QUESTION_QUIZ.getCommand())) {
            System.out.println(message);
            String parameters[] = message.split("/");
            Map<String, Integer> ids = new HashMap<>();
            for (String parameter : parameters) {
                ids.put(parameter.split(":")[0], Integer.parseInt(parameter.split(":")[1]));
            }
            int quizId = ids.get("quizId");
            int stepId = ids.get("stepId");
//            int answerId = ids.get("answerId");
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
                        " where step=" + (stepId + 1) + " and quizid=" + SpecialQuiz.START_QUIZ.getIdQuiz());
                if (resultSetForCheckStep.next()) {
                    int checkStepId = resultSetForCheckStep.getInt("nextStepQuizQuestionId");
                    if (checkStepId == stepId) {
                        isCorrectMessage = true;
                    }
                }
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    System.out.println(count);
                    if (count == 1) {
                        hasNext = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            if (isCorrectMessage) {
                if (hasNext) {
                    try {
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT * FROM quizquestion where step=" + (stepId + 1));
                        if (resultSet.next()) {
                            sendMessage.setText(resultSet.getString("questionText"));
                            statement.close();
                            statement = connection.createStatement();
                            statement.execute("update userquiz set nextstepquizquestionid=" + (stepId + 1));
                        }
                        sendMessage.setReplyMarkup(KeyBoardService.getNextVariablesOfAnswer(quizId, stepId + 1));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        Statement statement = connection.createStatement();
                        statement.execute("update userquiz SET nextstepquizquestionid=null,currentquizid=null where chatid=" + chatId);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    sendMessage.setText("Вы завершили опрос!");
                }
            } else {
                sendMessage.setText("Вы нажали не туда)");
            }

        }
        /*else if (hasCurrentQuiz()){
            sendMessage.setText("Вы нажали не туда)");
        }*/
        else {
            if (message.equals(Commands.START.getCommand())) {
                answerMessage = startMessage();
                sendMessage.setText(answerMessage);
                sendMessage.setReplyMarkup(KeyBoardService.wantStartTest());
            } else if (message.equals(Commands.PASSED_QUIZ_START)){
                answerMessage="В разработке";
                sendMessage.setText(answerMessage);
            }
            else if (message.equals(Commands.WANT_QUIZ_START.getCommand())) {
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
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM quizquestion where step=" + (stepId + 1));
                    if (resultSet.next()) {
                        sendMessage.setText(resultSet.getString("questionText"));
                    }
                    sendMessage.setReplyMarkup(KeyBoardService.getNextVariablesOfAnswer(SpecialQuiz.START_QUIZ.getIdQuiz(),
                            stepId + 1));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else if (message.matches(Commands.NEXT_STEP_QUIZ.getCommand())) {
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
                    ResultSet resultSet = statement.executeQuery("SELECT * from quizanswer where id="+answerId+" and \n" +
                            "quizquestionid=(select id from quizquestion\n" +
                            "where step="+stepId+" and\n" +
                            "quizid=(select currentquizid from userquiz where chatid="+chatId+"))");
                    if(resultSet.next()){
                        String resultText=resultSet.getString("resultText");
                        if(resultText.equals("")){
                            return getMessage("quizId:"+quizId+"/stepId:"+stepId);
                        };
                        sendMessage.setText(resultText);
                        sendMessage.setReplyMarkup(KeyBoardService.getNextQuestion(quizId,stepId));
                    }else{
                        sendMessage.setText("Вы нажали не туда)");
                    }
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                sendMessage.setText(answerMessage);
            }
        }


        return sendMessage;
    }

    private String startMessage() {
        return "Привет! Предлагаю пройти небольшой опрос, хочешь?";
    }

    private String listOfCommands() {
        return "Список доступных команд: /start";
    }

    private boolean hasCurrentQuiz(){
        Database database=new Database();
        Connection connection=database.getConnection();
        try {
            Statement statement=connection.createStatement();
            ResultSet resultSet= statement.executeQuery("select count(chatid)\n" +
                    "from userquiz where chatid="+chatId+" and nextstepquizquestionid is null and currentquizid is null;");
            if(resultSet.next()){
                if(resultSet.getInt("count")==1){
                    return false;
                }else{
                    return true;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}
