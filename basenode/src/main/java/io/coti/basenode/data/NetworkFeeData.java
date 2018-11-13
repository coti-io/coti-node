package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class NetworkFeeData extends OutputBaseTransactionData implements ITrustScoreNodeValidatable {
    @NotNull
    private List<TrustScoreNodeResultData> networkFeeTrustScoreNodeResult;

    private NetworkFeeData() {
        super();
    }

    public NetworkFeeData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Hash baseTransactionHash, SignatureData signature, Date createTime) {
        super(addressHash, amount, originalAmount, baseTransactionHash, signature, createTime);
    }

    public NetworkFeeData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Date createTime) {
        super(addressHash, amount, originalAmount, createTime);
    }

    @Override
    public List<TrustScoreNodeResultData> getTrustScoreNodeResult() {
        return networkFeeTrustScoreNodeResult;
    }

    @Override
    public void setTrustScoreNodeResult(List<TrustScoreNodeResultData> trustScoreNodeResult) {
        this.networkFeeTrustScoreNodeResult = trustScoreNodeResult;
    }
}
