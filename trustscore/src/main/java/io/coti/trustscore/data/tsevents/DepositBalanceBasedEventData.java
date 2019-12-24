package io.coti.trustscore.data.tsevents;

import io.coti.trustscore.http.InsertDepositBalanceBasedScoreRequest;
import lombok.Data;

@Data
public class DepositBalanceBasedEventData extends BalanceBasedEventData {

    private static final long serialVersionUID = 137332831704296705L;

    public DepositBalanceBasedEventData() {
    }

// Been created using reflection
    public DepositBalanceBasedEventData(InsertDepositBalanceBasedScoreRequest request) {
        super(request);
        this.amount = request.getAmount();
    }
}