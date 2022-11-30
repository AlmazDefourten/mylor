package ru.vshpit.model;

public enum SpecialQuiz {
    START_QUIZ(1); //default 1, set before start bot in initializationQuiz()

    private int idQuiz;

    SpecialQuiz(int idQuiz) {
        this.idQuiz = idQuiz;
    }

    public int getIdQuiz() {
        return idQuiz;
    }

    public void setIdQuiz(int idQuiz) {
        this.idQuiz = idQuiz;
    }
}
