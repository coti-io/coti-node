package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HighFrequencyEventScore extends EventScore {

    private double standardChargeBackRate;

    private long term;
}
