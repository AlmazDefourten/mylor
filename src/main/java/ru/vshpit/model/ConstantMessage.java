package ru.vshpit.model;

public enum ConstantMessage {
    GREETING("Вы хотите пройти диагностику?"),
    TEXT_FOR_START_BUTTON("Привет! Чтобы начать введите команду /start"),
    UNCORRECTED_MESSAGE("Вы нажали не туда"),
    WANT_OTHER_QUIZS("Хотите пройти другие опросы?"),
    DONT_HAS_QUIZS("Других опросов пока нету."),
    LIST_OF_QUIZS("Список опросов:"),
    NEXT("Далее"),
    WANT_DIAGNOSTIC("Я хочу пройти диагностику"),
    I_RUN_DIAGNOSTIC("Я уже проходил диагностику"),
    CHOOSE_DIAGNOSTIC("Выберите диагностику"),
    LOOK_RESULT("Посмотреть рекомендации"),
    END_DIAGNOSTIC("Вы завершили диагностику!");

    private String message;

    ConstantMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
