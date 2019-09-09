package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class InsertChargeBackFrequencyBasedScoreRequest extends SignedRequest {
    private static final long serialVersionUID = -2115495911453522146L;
    @NotNull
    public Hash eventIdentifier;
    @NotNull
    public Hash transactionHash;
    @NotNull
    public BigDecimal amount;
}