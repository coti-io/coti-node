package io.coti.trustscore.data.Enums;

public enum BehaviorEventsScoreType {

    DOUBLE_SPENDING("DoubleSpending"),

    INCORRECT_TRANSACTION("IncorrectTransaction"),

    FILLING_QUESTIONNAIRE("FillingTheQuestionnaire");

    private String text;

    BehaviorEventsScoreType(String text) {
        this.text = text;
    }

    public static BehaviorEventsScoreType enumFromString(String text) {
        for (BehaviorEventsScoreType value : BehaviorEventsScoreType.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format("got event name {}, which not exists", text));
    }

    @Override
    public String toString() {
        return text;
    }
}