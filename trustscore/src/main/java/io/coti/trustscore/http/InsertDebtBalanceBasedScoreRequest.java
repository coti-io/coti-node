package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.tsenums.DebtBalanceBasedEventRequestType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class InsertDebtBalanceBasedScoreRequest extends SignedRequest {
    @NotNull
    private DebtBalanceBasedEventRequestType eventType;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Hash eventIdentifier;
    @NotNull
    private Hash otherUserHash;
}