package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class CompensableEventScore extends EventScore {

    private int term;
    private double weight1;
    private double weight2;
    private String contribution;
    private String fine;
    private String fineDailyChange;

}