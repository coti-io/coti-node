package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class FundDistributionBalanceData {

    protected Fund fund;
    protected BigDecimal reservedAmount;
    protected BigDecimal preBalance;
    protected BigDecimal balance;
    protected Instant createTime;
    private Hash hash;

    public FundDistributionBalanceData(Fund fund, BigDecimal reservedAmount) {
        this.fund = fund;
        this.reservedAmount = reservedAmount;
        this.createTime = Instant.now();
        this.hash = fund.getFundHash();
    }

}
