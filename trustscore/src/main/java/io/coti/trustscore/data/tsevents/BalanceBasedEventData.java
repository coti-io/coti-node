package io.coti.trustscore.data.tsevents;

import io.coti.trustscore.http.SignedRequest;
import lombok.Data;

import java.math.BigDecimal;

@Data
public abstract class BalanceBasedEventData extends SignedEventData {
    private static final long serialVersionUID = -160695542578319293L;
    protected BigDecimal amount;

    public BalanceBasedEventData() {
    }

// Been created using reflection
    public BalanceBasedEventData(SignedRequest request) {
        super(request);
    }
}