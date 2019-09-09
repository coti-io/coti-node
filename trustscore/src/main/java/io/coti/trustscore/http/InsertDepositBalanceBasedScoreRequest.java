package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.scoreenums.DepositBalanceBasedScoreRequestType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class InsertDepositBalanceBasedScoreRequest extends SignedRequest {
    private static final long serialVersionUID = -7546720663722991463L;
    @NotNull
    public DepositBalanceBasedScoreRequestType eventType;
    @NotNull
    public BigDecimal amount;
    @NotNull
    public Hash eventIdentifier;
}