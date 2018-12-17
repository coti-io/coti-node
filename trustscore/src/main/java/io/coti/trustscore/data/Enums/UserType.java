package io.coti.trustscore.data.Enums;

public enum UserType {
    MERCHANT("merchant"),
    CONSUMER("consumer"),
    FULL_NODE("fullnode");

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
        throw new IllegalArgumentException(String.format("got user type name {}, which not exists", text));
    }

    @Override
    public String toString() {
        return text;
    }
}
