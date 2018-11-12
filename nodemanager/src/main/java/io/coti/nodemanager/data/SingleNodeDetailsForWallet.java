package io.coti.nodemanager.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class SingleNodeDetailsForWallet {
    private String fullHttpAddress;
    private Double feePercentage;
    private Double trustScore;

    public SingleNodeDetailsForWallet(String fullHttpAddress, Double feePercentage, Double trustScore) {
        this.fullHttpAddress = fullHttpAddress;
        this.feePercentage = feePercentage;
        this.trustScore = trustScore;
    }
}
