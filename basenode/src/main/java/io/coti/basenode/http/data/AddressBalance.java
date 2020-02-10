package io.coti.basenode.http.data;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AddressBalance implements Serializable {

    private BigDecimal addressBalance;
    private BigDecimal addressPreBalance;

    public AddressBalance(BigDecimal addressBalance, BigDecimal addressPreBalance) {
        this.addressBalance = addressBalance;
        this.addressPreBalance = addressPreBalance;
    }
}
