package io.coti.financialserver.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundDistributionBalanceData {

    protected Fund fund;
    protected BigDecimal reservedAmount;


    public FundDistributionBalanceData(Fund fund, BigDecimal reservedAmount) {
        this.fund = fund;
        this.reservedAmount = reservedAmount;
    }

}
