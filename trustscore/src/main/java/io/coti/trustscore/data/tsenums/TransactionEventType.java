package io.coti.trustscore.data.tsenums;

public enum TransactionEventType {

    SENDER_EVENT("SenderEvent"),
    RECEIVER_EVENT("ReceiverEvent"),
    SENDER_NEW_ADDRESS_EVENT("SenderNewAddressEvent");

    private String text;

    TransactionEventType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
