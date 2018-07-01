package io.coti.cotinode.http.websocket;

public class AddressSubscription {
    public String message;

    public AddressSubscription(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
