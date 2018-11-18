package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class RollingReserveData extends OutputBaseTransactionData implements ITrustScoreNodeValidatable {
    @NotNull
    private List<TrustScoreNodeResultData> rollingReserveTrustScoreNodeResult;

    private RollingReserveData() {
        super();
    }

    public RollingReserveData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Date createTime) {
        super(addressHash, amount, originalAmount, createTime);
    }

    @Override
    public List<TrustScoreNodeResultData> getTrustScoreNodeResult() {
        return rollingReserveTrustScoreNodeResult;
    }

    @Override
    public void setTrustScoreNodeResult(List<TrustScoreNodeResultData> trustScoreNodeResult) {
        this.rollingReserveTrustScoreNodeResult = trustScoreNodeResult;
    }
}
