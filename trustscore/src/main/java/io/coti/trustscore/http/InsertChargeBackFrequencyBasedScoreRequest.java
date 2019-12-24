package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class InsertChargeBackFrequencyBasedScoreRequest extends SignedRequest {
    @NotNull
    private Hash eventIdentifier;
    @NotNull
    private Hash transactionHash;
    @NotNull
    private BigDecimal amount;
}