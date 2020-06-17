package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class HighFrequencyEventScore extends EventScore {

    private String linearFunction;
    private double standardChargeBackRate;
    private String contribution;
    private String term;
}
