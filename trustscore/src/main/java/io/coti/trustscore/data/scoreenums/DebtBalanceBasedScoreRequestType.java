package io.coti.trustscore.data.scoreenums;

import io.coti.trustscore.data.scoreevents.*;

public enum DebtBalanceBasedScoreRequestType {
    DEBT("DEBT", DebtBalanceBasedScoreData.class),
    CLOSEDEBT("CLOSEDEBT", CloseDebtBalanceBasedScoreData.class);

    private String text;
    public Class score;

    DebtBalanceBasedScoreRequestType(String text , Class score) {
        this.text = text;
        this.score = score;
    }

    public static DebtBalanceBasedScoreRequestType enumFromString(String text) {
        for (DebtBalanceBasedScoreRequestType value : DebtBalanceBasedScoreRequestType.values()) {
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
