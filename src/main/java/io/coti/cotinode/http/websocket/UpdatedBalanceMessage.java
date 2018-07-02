package io.coti.cotinode.http.websocket;

import io.coti.cotinode.data.Hash;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatedBalanceMessage {
    public String message;
    public String addressHash;
    public BigDecimal balance;

    public UpdatedBalanceMessage(Hash addressHash, BigDecimal balance) {
        this.addressHash = addressHash.toHexString();
        this.balance = balance;
        this.message = "Balance Updated!";
    }
}
