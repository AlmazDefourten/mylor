package ru.vshpit.service;


import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailService {
    private static List<Address> to;
    private static String from;
    private static String host;

    public void initialization() {
        to = new ArrayList<>();
        try {
            to.add(new InternetAddress("34783tgs@mail.ru"));
        } catch (AddressException e) {
            System.out.println("Проверьте правильность email адреса");
            e.printStackTrace();
        }
        from = "34783tgs@mail.ru";
        host = "168.100.189.0";
    }

    private Session getSession() {
        // Получить свойства системы
        Properties properties = System.getProperties();

        // Настроить почтовый сервер
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.port","28");
//        properties.put("mail.smtp.auth"               , "true"     );
//        properties.put("mail.smtp.ssl.enable"         , "true"     );
//        properties.put("mail.smtp.socketFactory.class",
//                "javax.net.ssl.SSLSocketFactory");

        // Получение объекта Session по умолчанию
        Session session = Session.getDefaultInstance(properties);
        return session;
    }

    public void testSendMessage() {
        Session session = getSession();
        try {
            // Создание объекта MimeMessage по умолчанию
            MimeMessage message = new MimeMessage(session);

            // Установить От: поле заголовка
            message.setFrom(new InternetAddress(from));

            // Установить Кому: поле заголовка
            message.addRecipients(Message.RecipientType.TO, to.toArray(new InternetAddress[to.size()]));

            // Установить тему: поле заголовка
            message.setSubject("Это тема письма!");

            // Теперь установите фактическое сообщение
            message.setText("Это актуальное сообщение");

            // Отправить сообщение
            Transport.send(message);
            System.out.println("Сообщение успешно отправлено....");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
