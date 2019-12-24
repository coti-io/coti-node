package io.coti.trustscore.data.tsevents;

import io.coti.trustscore.http.InsertDepositBalanceBasedScoreRequest;
import lombok.Data;

@Data
public class CloseDepositBalanceBasedEventData extends BalanceBasedEventData {

    private static final long serialVersionUID = -317369635623480823L;

    public CloseDepositBalanceBasedEventData() {
    }

// Been created using reflection
    public CloseDepositBalanceBasedEventData(InsertDepositBalanceBasedScoreRequest request) {
        super(request);
        this.amount = request.getAmount();
    }
}