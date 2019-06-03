package io.coti.financialserver.http.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundDistributionBalanceResultData {

    private String fundName;
    private BigDecimal balance;
    private BigDecimal preBalance;
    private BigDecimal lockedBalance;

    public FundDistributionBalanceResultData(String fundName, BigDecimal balance, BigDecimal preBalance, BigDecimal lockedBalance) {
        this.fundName = fundName;
        this.balance = balance;
        this.preBalance = preBalance;
        this.lockedBalance = lockedBalance;
    }
}
