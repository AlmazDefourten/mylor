package ru.vshpit.service;

import org.telegram.telegrambots.meta.api.objects.Message;

public class CommandService {

    public String getMessage(String message){
        switch (message){
            case "/start":
                return startMessage();
            default:
                return listOfCommands();
        }
    }

    private String startMessage(){
        return "Приветсвую тебя";
    }

    private String listOfCommands(){
        return "Список доступных команд: /start";
    }
}
