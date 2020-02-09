package io.coti.fullnode.websocket.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatedBalanceMessage {

    private final BigDecimal preBalance;
    private String message;
    private String addressHash;
    private BigDecimal balance;

    public UpdatedBalanceMessage(Hash addressHash, BigDecimal balance, BigDecimal preBalance) {
        this.addressHash = addressHash.toHexString();
        this.balance = balance;
        this.preBalance = preBalance;
        this.message = "Balance Updated!";
    }
}
