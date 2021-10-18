package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RollingReserveData extends OutputBaseTransactionData implements ITrustScoreNodeValidatable {

    private static final long serialVersionUID = -8006331684448356440L;
    @NotNull
    private List<TrustScoreNodeResultData> rollingReserveTrustScoreNodeResult;
    private BigDecimal reducedAmount;

    private RollingReserveData() {
        super();
    }

    public RollingReserveData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, BigDecimal reducedAmount, Instant createTime) {
        super(addressHash, amount, originalAmount, createTime);
        this.setReducedAmount(reducedAmount);
    }

    @Override
    public List<TrustScoreNodeResultData> getTrustScoreNodeResult() {
        return rollingReserveTrustScoreNodeResult;
    }

    @Override
    public void setTrustScoreNodeResult(List<TrustScoreNodeResultData> trustScoreNodeResult) {
        this.rollingReserveTrustScoreNodeResult = trustScoreNodeResult;
    }

    public void setReducedAmount(BigDecimal reducedAmount) {
        if (reducedAmount == null || reducedAmount.signum() <= 0) {
            throw new IllegalStateException("Reduced amount can not have non positive amount");
        }
        this.reducedAmount = reducedAmount;
    }
}
