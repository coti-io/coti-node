package io.coti.trustscore.http.data;

import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.NetworkFeeData;
import io.coti.basenode.data.TrustScoreNodeResultData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class NetworkFeeResponseData {

    private String hash;
    private String amount;
    private String originalAmount;
    private String reducedAmount;
    private String addressHash;
    private Date createTime;
    private String name;
    private List<TrustScoreNodeResultResponseData> networkFeeTrustScoreNodeResult;

    public NetworkFeeResponseData(NetworkFeeData networkFeeData) {
        this.hash = networkFeeData.getHash().toString();
        this.amount = networkFeeData.getAmount().toString();
        this.originalAmount = networkFeeData.getOriginalAmount().toString();
        this.reducedAmount = networkFeeData.getReducedAmount().toString();
        this.addressHash = networkFeeData.getAddressHash().toString();
        this.createTime = networkFeeData.getCreateTime();
        this.name = BaseTransactionName.getName(NetworkFeeData.class).name();
        this.networkFeeTrustScoreNodeResult = new ArrayList<>();

        for (TrustScoreNodeResultData trustScoreNodeResultData : networkFeeData.getTrustScoreNodeResult()) {
            networkFeeTrustScoreNodeResult.add(new TrustScoreNodeResultResponseData(trustScoreNodeResultData));
        }
    }
}



