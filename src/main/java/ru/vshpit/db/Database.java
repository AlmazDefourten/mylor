package ru.vshpit.db;

import ru.vshpit.Application;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private static Connection connection=InitConnection();

    public static Connection getConnection(){
        return connection;
    }

    private static Connection InitConnection(){
        Properties properties=new Properties();
        try {
            properties.load(Application.class.getClassLoader().getResourceAsStream("database.properties"));
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.user");
            String pass = properties.getProperty("db.password");
            try {
                connection = DriverManager.getConnection(url, user, pass);
                System.out.println("Connected to " + url);
            } catch (SQLException e) {
                System.out.println("ERROR: not connected to database.");
            }
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error to create connection");
    }
}
