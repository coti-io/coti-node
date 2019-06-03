package io.coti.financialserver.data;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundDistributionReservedBalanceData extends ReservedBalanceData {

    private Fund fund;

    private FundDistributionReservedBalanceData() {
        super();
    }

    public FundDistributionReservedBalanceData(Fund fund, BigDecimal reservedAmount) {
        super(reservedAmount);
        this.fund = fund;
    }

}
