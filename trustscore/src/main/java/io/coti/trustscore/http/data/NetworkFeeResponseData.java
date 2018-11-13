package io.coti.trustscore.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.NetworkFeeData;
import io.coti.basenode.data.TrustScoreNodeResultData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class NetworkFeeResponseData {

    private String hash;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private String addressHash;
    private Date createTime;
    private String name;
    private BigDecimal networkFee;
    private List<TrustScoreNodeResultData> trustScoreNodeResultData;

    public NetworkFeeResponseData(NetworkFeeData networkFeeData) {
        this.hash = networkFeeData.getHash().toString();
        this.amount = networkFeeData.getAmount();
        this.originalAmount = networkFeeData.getOriginalAmount();
        this.addressHash = networkFeeData.getAddressHash().toString();
        this.createTime = networkFeeData.getCreateTime();
        this.name = BaseTransactionName.getName(NetworkFeeData.class).name();
        this.trustScoreNodeResultData = networkFeeData.getNetworkFeeTrustScoreNodeResult();
    }
}



