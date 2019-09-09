package io.coti.trustscore.data.scoreevents;

import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertDepositBalanceBasedScoreRequest;
import lombok.Data;

@Data
public class CloseDepositBalanceBasedScoreData extends BalanceBasedScoreData {

    private static final long serialVersionUID = -317369635623480823L;

    public CloseDepositBalanceBasedScoreData() {
    }

    public CloseDepositBalanceBasedScoreData(InsertDepositBalanceBasedScoreRequest request) {
        super(request, FinalScoreType.CLOSEDEPOSIT);
        this.amount = request.getAmount();
    }
}