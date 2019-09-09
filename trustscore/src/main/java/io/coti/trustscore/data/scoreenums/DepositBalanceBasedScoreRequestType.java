package io.coti.trustscore.data.scoreenums;

import io.coti.trustscore.data.scoreevents.CloseDepositBalanceBasedScoreData;
import io.coti.trustscore.data.scoreevents.DepositBalanceBasedScoreData;

public enum DepositBalanceBasedScoreRequestType {
    DEPOSIT("DEPOSIT", DepositBalanceBasedScoreData.class),
    CLOSEDEPOSIT("CLOSEDEPOSIT", CloseDepositBalanceBasedScoreData.class);

    private String text;
    public Class score;

    DepositBalanceBasedScoreRequestType(String text , Class score) {
        this.text = text;
        this.score = score;
    }

    public static DepositBalanceBasedScoreRequestType enumFromString(String text) {
        for (DepositBalanceBasedScoreRequestType value : DepositBalanceBasedScoreRequestType.values()) {
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
