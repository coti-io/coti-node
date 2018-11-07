package io.coti.trustscore.data.Enums;

public enum UserType {
    MERCHANT("merchant"),
    WALLET("wallet"),
    FULL_NODE("node");

    private String text;

    UserType(String text) {
        this.text = text;
    }

    public static UserType enumFromString(String text) {
        for (UserType value: UserType.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return text;
    }
}
