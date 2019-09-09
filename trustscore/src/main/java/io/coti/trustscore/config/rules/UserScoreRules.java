package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserScoreRules {

    private String userType;
    private double weight;
    private long semiDecay;
    private long term;
    private double standardChargeBackRate;
    private double level08;
    private double weight1;
    private double weight2;
    private int period;
    private int limit;

    private double amountWeight = 0;
    private double amountWeight1 = 0;
    private double amountWeight2 = 0;
    private long amountSemiDecay = 0;
    private double numberWeight = 0;
    private double numberWeight1 = 0;
    private double numberWeight2 = 0;
    private double numberLevel08 = 0;
    private long numberSemiDecay = 0;
    private double turnoverWeight = 0;
    private double turnoverLevel08 = 0;
    private long turnoverSemiDecay = 0;
    private double balanceWeight = 0;
    private double balanceLevel08 = 0;
    private long balanceSemiDecay = 0;

}
