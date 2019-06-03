package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReservedBalanceResponseData {

    private String address;
    private BigDecimal lockupBalance;

    public ReservedBalanceResponseData(Hash address, BigDecimal reservedBalance) {
        this.address = address.toString();
        this.lockupBalance = reservedBalance;
    }
}
