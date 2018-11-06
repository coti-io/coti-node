package io.coti.nodemanager.data;

import lombok.Data;

@Data
public class SingleNodeDetailsForWallet {
    private String fullHttpAddress;
    private Double fee;
    private Double trustScore;

    public SingleNodeDetailsForWallet(String fullHttpAddress, Double fee, Double trustScore) {
        this.fullHttpAddress = fullHttpAddress;
        this.fee = fee;
        this.trustScore = trustScore;
    }
}
