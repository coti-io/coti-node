package io.coti.nodemanager.http.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.coti.basenode.data.Hash;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class SingleNodeDetailsForWallet {

    private Hash nodeHash;
    private String fullHttpAddress;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double feePercentage;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double trustScore;

    public SingleNodeDetailsForWallet(Hash nodeHash, String fullHttpAddress, Double feePercentage, Double trustScore) {
        this.nodeHash = nodeHash;
        this.fullHttpAddress = fullHttpAddress;
        this.feePercentage = feePercentage;
        this.trustScore = trustScore;
    }
}
