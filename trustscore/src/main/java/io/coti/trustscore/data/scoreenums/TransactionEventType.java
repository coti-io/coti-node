package io.coti.trustscore.data.scoreenums;

public enum TransactionEventType {

    SENDER_EVENT("SenderEvent"),
    RECEIVER_EVENT("ReceiverEvent"),
    SENDER_NEW_ADDRESS_EVENT("SenderNewAddressEvent");

    private String text;

    TransactionEventType(String text) {
        this.text = text;
    }

    public static TransactionEventType enumFromString(String text) {
        for (TransactionEventType value : TransactionEventType.values()) {
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
