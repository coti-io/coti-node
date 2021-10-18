package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReservedBalanceResponseData implements IResponseData {

    private String address;
    private BigDecimal lockupBalance;

    public ReservedBalanceResponseData(Hash address, BigDecimal reservedBalance) {
        this.address = address.toString();
        this.lockupBalance = reservedBalance;
    }
}
