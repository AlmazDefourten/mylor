package ru.vshpit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.vshpit.bot.TelegramBot;
import ru.vshpit.db.Database;
import ru.vshpit.model.Commands;
import ru.vshpit.service.KeyBoardService;

import javax.management.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Properties;
import java.util.Scanner;

public class Application {
    public static void main(String[] args) throws SQLException {
        initializationSqlSchema();
        initializationQuiz();
        initializationBot();
    }

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
                Statement statement=connection.createStatement();
                NodeList list = document.getElementsByTagName("quiz");
                for (int temp = 0; temp < list.getLength(); temp++) {
                    Node node = list.item(temp);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element=(Element) node;

                        NodeList listAnswers=element.getElementsByTagName("answer");
                        System.out.println(listAnswers.getLength());
                    }
                }
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