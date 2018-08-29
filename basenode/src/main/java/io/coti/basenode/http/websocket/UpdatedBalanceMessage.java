package io.coti.basenode.http.websocket;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatedBalanceMessage {
    private final BigDecimal preBalance;
    public String message;
    public String addressHash;
    public BigDecimal balance;

    public UpdatedBalanceMessage(Hash addressHash, BigDecimal balance, BigDecimal preBalance) {
        this.addressHash = addressHash.toHexString();
        this.balance = balance;
        this.preBalance = preBalance;

        this.message = "Balance Updated!";
    }
}
