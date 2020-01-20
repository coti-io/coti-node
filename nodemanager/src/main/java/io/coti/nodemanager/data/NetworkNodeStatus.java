package io.coti.nodemanager.data;

public enum NetworkNodeStatus {
    ACTIVE ("active"),
    INACTIVE ("inactive"),
    BANNED ("banned");

    private String text;

    NetworkNodeStatus(String text) {
        this.text = text;
    }

    public static NetworkNodeStatus enumFromString(String text) {
        for (NetworkNodeStatus value : NetworkNodeStatus.values()) {
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
