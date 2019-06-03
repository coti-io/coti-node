package io.coti.financialserver.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReservedBalanceData {

    protected BigDecimal reservedAmount;

    public ReservedBalanceData() {
    }

    public ReservedBalanceData(BigDecimal reservedAmount) {
        this.reservedAmount = reservedAmount;
    }
}
