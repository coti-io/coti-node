package io.coti.financialserver.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
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
