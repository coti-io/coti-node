package io.coti.basenode.http.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressBalance {

    public BigDecimal balance;
    public BigDecimal preBalance;

    public AddressBalance(BigDecimal balance, BigDecimal preBalance) {
        this.balance = balance;
        this.preBalance = preBalance;
    }
}
