package ru.vshpit.model;

public enum Commands {
    START("/start"),
    NEXT_STEP_QUIZ("quizId:\\d*\\/stepId:\\d*\\/answerId:\\d*"),
    NEXT_QUESTION_QUIZ("quizId:\\d*\\/stepId:\\d*"),
    WANT_QUIZ_START("/wantQuizStart"),
    PASSED_QUIZ_START("/passedQuizStart");
    private String command;

    Commands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
