package io.coti.trustscore.data.scoreevents;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertDebtBalanceBasedScoreRequest;
import lombok.Data;

@Data
public class CloseDebtBalanceBasedScoreData extends BalanceBasedScoreData {

    private static final long serialVersionUID = -7874193033558594029L;
    private Hash plaintiffUserHash;

    public CloseDebtBalanceBasedScoreData() {
    }

    public CloseDebtBalanceBasedScoreData(InsertDebtBalanceBasedScoreRequest request) {
        super(request, FinalScoreType.CLOSEDEBT);
        this.plaintiffUserHash = request.getOtherUserHash();
        this.amount = request.getAmount();
    }
}