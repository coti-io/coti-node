package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserNetworkFeeByTrustScoreRange {

    private String type;
    private double limit;
    private double minRate;
    private double maxRate;
    private double feeRate;
}
