package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateDistributionAmountResponse extends BaseResponse {

    private String distributionHash;
    private BigDecimal oldAmount;
    private BigDecimal newAmount;

    public UpdateDistributionAmountResponse(Hash distributionHash, BigDecimal oldAmount, BigDecimal newAmount) {
        this.distributionHash = distributionHash.toString();
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
    }
}
