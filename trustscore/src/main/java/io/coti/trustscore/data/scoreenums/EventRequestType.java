package io.coti.trustscore.data.scoreenums;

import io.coti.trustscore.data.scoreevents.*;

public enum EventRequestType {
    FALSEQUESTIONNAIRE("FALSEQUESTIONNAIRE", FalseQuestionnaireEventScoreData.class),
    DOUBLESPENDING("DOUBLESPENDING", DoubleSpendingEventScoreData.class),
    INVALIDTX("INVALIDTX", InvalidTxEventScoreData.class),
    CLAIM("CLAIM", ClaimFrequencyBasedScoreData.class);

    private String text;
    public Class score;

    EventRequestType(String text , Class score) {
        this.text = text;
        this.score = score;
    }

    public static EventRequestType enumFromString(String text) {
        for (EventRequestType value : EventRequestType.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format("Not existing event name {}", text));
    }

    @Override
    public String toString() {
        return text;
    }
}
