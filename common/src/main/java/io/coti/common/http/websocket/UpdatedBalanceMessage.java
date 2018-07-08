package io.coti.common.http.websocket;

import io.coti.common.data.Hash;
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
