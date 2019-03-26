package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserNetworkFeeByTrustScoreRange {

    private String type;
    private double limit;
    private BigDecimal minRate;
    private BigDecimal maxRate;
    private BigDecimal feeRate;
}
