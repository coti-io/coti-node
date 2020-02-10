package io.coti.trustscore.data.Enums;

public enum UserType {
    CONSUMER("consumer"),
    MERCHANT("merchant"),
    ARBITRATOR("arbitrator"),
    FULL_NODE("fullnode"),
    DSP_NODE("dspnode"),
    TRUST_SCORE_NODE("trustscorenode");

    private String text;

    UserType(String text) {
        this.text = text;
    }

    public static UserType enumFromString(String text) {
        for (UserType value : UserType.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format("User type %s doesn't exist", text));
    }

    @Override
    public String toString() {
        return text;
    }
}
