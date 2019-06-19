package io.coti.trustscore.config.rules;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEventScore extends EventScore {

    private double level08;
    private double atanh08;

}
