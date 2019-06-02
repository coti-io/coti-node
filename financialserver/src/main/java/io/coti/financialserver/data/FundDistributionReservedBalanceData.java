package io.coti.financialserver.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundDistributionReservedBalanceData {

    protected Fund fund;
    protected BigDecimal reservedAmount;


    public FundDistributionReservedBalanceData(Fund fund, BigDecimal reservedAmount) {
        this.fund = fund;
        this.reservedAmount = reservedAmount;
    }

}
