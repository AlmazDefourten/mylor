package ru.vshpit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.vshpit.bot.TelegramBot;
import ru.vshpit.db.Database;
import ru.vshpit.model.Commands;
import ru.vshpit.model.SpecialQuiz;
import ru.vshpit.service.EmailService;
import ru.vshpit.service.KeyBoardService;

import javax.management.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Comparator;
import java.util.Properties;
import java.util.Scanner;

public class Application {
    public static void main(String[] args){
//        initializationSqlSchema();
//        initializationQuiz();
//        initializationBot();
        EmailService emailService=new EmailService();
        emailService.initialization();
        emailService.testSendMessage();
    }

    //спарсить опросы в бд из quizs.xml
    private static void initializationQuiz(){;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder=dbf.newDocumentBuilder();
            Document document=documentBuilder.parse(new File("src/main/resources/quizs.xml"));
            document.getDocumentElement().normalize();
            System.out.println("Root Element :" + document.getDocumentElement().getNodeName());
            System.out.println("------");
            Database database=new Database();
            Connection connection=database.getConnection();
            try {
                NodeList list = document.getElementsByTagName("quiz");
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element=(Element) node;
                        Statement statement=connection.createStatement();
                        statement.execute("insert into quiz (title) values('"+element.getElementsByTagName("title").item(0).getTextContent() +"');");
                        ResultSet resultSet= statement.executeQuery("SELECT currval(pg_get_serial_sequence('quiz','id'));");
                        int quizId=0;
                        if(resultSet.next()){
                            quizId=resultSet.getInt("currval");
                        }
                        if(element.hasAttribute("isMain")){
                            SpecialQuiz.START_QUIZ.setIdQuiz(quizId);  //установить id стартового опроса, по дефолту 1 (Обязательно!!!)
                        }
                        NodeList listQuestion=element.getElementsByTagName("question");
                        for(int y=0;y<listQuestion.getLength();y++){
                            Node nodeY=listQuestion.item(y);
                            if(nodeY.getNodeType()==Node.ELEMENT_NODE){
                                Element elementY=(Element) nodeY;
                                statement=connection.createStatement();
                                statement.execute("insert into quizquestion (quizid, questiontext, step) values("+quizId+",'"+elementY.getElementsByTagName("questionText").item(0).getTextContent()+"',"+(y+1)+");");
                                ResultSet resultSetY=statement.executeQuery("SELECT currval(pg_get_serial_sequence('quizquestion','id'));");
                                int questionId=0;
                                if(resultSetY.next()){
                                    questionId=resultSetY.getInt("currval");
                                }
                                NodeList listAnswers=elementY.getElementsByTagName("answer");
                                for(int j=0;j<listAnswers.getLength();j++){
                                    Node nodeJ=listAnswers.item(j);
                                    if(node.getNodeType() == Node.ELEMENT_NODE){
                                        Element elementJ=(Element) nodeJ;
                                        statement=connection.createStatement();
                                        statement.execute("insert into quizanswer (quizquestionid, answertext, resulttext) values ("+questionId+",'"+elementJ.getElementsByTagName("answerText").item(0).getTextContent()+"','"+elementJ.getElementsByTagName("answerResultText").item(0).getTextContent()+"');");
                                    }
                                }
                            }
                        }

                    }
                }
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    //создать(пересоздать) базовые таблицы и обновить данные
    private static void initializationSqlSchema(){
        try {
            Scanner scanner = new Scanner(Application.class.getClassLoader().getResourceAsStream("schema.sql"));
            Database database=new Database();
            Connection connection=database.getConnection();
            Statement statement= connection.createStatement();
            String query="";
            while (scanner.hasNextLine()){
                query+=scanner.nextLine();
            }
            statement.execute(query);
            connection.close();
            System.out.println("SQl таблицы созданы");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("check database");
        }

    }

    //запустить тг бота
    private static void initializationBot(){
        Properties properties = new Properties();
        try {
            properties.load(Application.class.getClassLoader().getResourceAsStream("telegrambot.properties"));
            String botName=properties.getProperty("bot.name");
            String botToken=properties.getProperty("bot.token");
            TelegramBot telegramBot=new TelegramBot(botName,botToken);
            telegramBot.botConnect();
            System.out.println("telegram bot запущен");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}