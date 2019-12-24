package io.coti.trustscore.data.contributiondata;


import lombok.Data;

import java.io.Serializable;

@Data
public class DocumentDecayedContributionData implements Serializable {

    private static final long serialVersionUID = 6243956025013924269L;
    private double originalTrustScore;
    private double decayedTrustScore;

    public DocumentDecayedContributionData(double originalTrustScore, double decayedTrustScore) {
        this.originalTrustScore = originalTrustScore;
        this.decayedTrustScore = decayedTrustScore;
    }
}
