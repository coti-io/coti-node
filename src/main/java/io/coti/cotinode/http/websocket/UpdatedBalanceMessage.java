package io.coti.cotinode.http.websocket;

import io.coti.cotinode.data.Hash;

public class UpdatedBalanceMessage {
    public String message;
    public String addressHash;
    public double balance;

    public UpdatedBalanceMessage(Hash addressHash, double balance) {
        this.addressHash = addressHash.toHexString();
        this.balance = balance;
        this.message = "Balance Updated!";
    }

    public String getAddress() {
        return addressHash;
    }

    public double getBalance() {
        return balance;
    }
}
