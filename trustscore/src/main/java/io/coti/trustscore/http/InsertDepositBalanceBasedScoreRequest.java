package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.tsenums.DepositBalanceBasedEventRequestType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class InsertDepositBalanceBasedScoreRequest extends SignedRequest {
    @NotNull
    private DepositBalanceBasedEventRequestType eventType;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Hash eventIdentifier;
}