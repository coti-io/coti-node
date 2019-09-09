package io.coti.trustscore.data.scoreevents;

import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertDepositBalanceBasedScoreRequest;
import lombok.Data;

@Data
public class DepositBalanceBasedScoreData extends BalanceBasedScoreData {

    private static final long serialVersionUID = 137332831704296705L;

    public DepositBalanceBasedScoreData() {
    }

    public DepositBalanceBasedScoreData(InsertDepositBalanceBasedScoreRequest request) {
        super(request, FinalScoreType.DEPOSIT);
        this.amount = request.getAmount();
    }
}