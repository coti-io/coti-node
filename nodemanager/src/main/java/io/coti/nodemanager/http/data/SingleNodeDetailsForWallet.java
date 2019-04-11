package io.coti.nodemanager.http.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.coti.basenode.data.FeeData;
import io.coti.basenode.data.Hash;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class SingleNodeDetailsForWallet {

    private Hash nodeHash;
    private String fullHttpAddress;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FeeData feeData;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double trustScore;

    public SingleNodeDetailsForWallet(Hash nodeHash, String fullHttpAddress, FeeData feeData, Double trustScore) {
        this.nodeHash = nodeHash;
        this.fullHttpAddress = fullHttpAddress;
        this.feeData = feeData;
        this.trustScore = trustScore;
    }
}
