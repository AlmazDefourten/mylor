package ru.vshpit.model;

public enum Commands {
    START("/start"),
    NEXT_STEP_QUIZ("quizId:\\d*\\/stepId:\\d*\\/answerId:\\d*"),
    NEXT_QUESTION_QUIZ("quizId:\\d*\\/stepId:\\d*"),
    WANT_QUIZ_START("/wantQuizStart"),
    PASSED_QUIZ_START("/passedQuizStart"),
    SEE_OTHER_QUIZ("/seeOtherQuiz"),
    WANT_DIAGNOSTICS("/wantDiagnostics"),
    LOOK_RESULT("/lookResult"),
    WANT_OTHER_QUIZ("/wantOtherQuiz/quizId:\\d*");

    private String command;

    Commands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
