package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompensableEventScore extends EventScore {

    private long term;
    private double weight1;
    private double weight2;
}