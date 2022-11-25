package ru.vshpit;

import ru.vshpit.bot.TelegramBot;
import ru.vshpit.db.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Properties;
import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        initializationBot();
        initializationSqlSchema();
    }

    private static void initializationSqlSchema(){
        try {
            Properties properties = new Properties();
            Scanner scanner = new Scanner(Application.class.getClassLoader().getResourceAsStream("schema.sql"));
            Connection connection=Database.getConnection();
            Statement statement= connection.createStatement();
            while (scanner.hasNextLine()){
                statement.execute(scanner.nextLine());
            }
            connection.close();
            System.out.println("SQl таблицы созданы");
        } catch (SQLException e) {
            e.printStackTrace();
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