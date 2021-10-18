package io.coti.trustscore.config.rules;

import lombok.Data;

@Data
public abstract class EventScore {

    private String name;
    private double weight;
    private String decay;
}
