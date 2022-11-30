package ru.vshpit.model;

public enum SpecialQuiz {
    START_QUIZ(1);

    private int idQuiz;

    SpecialQuiz(int idQuiz) {
        this.idQuiz = idQuiz;
    }

    public int getIdQuiz() {
        return idQuiz;
    }
}
