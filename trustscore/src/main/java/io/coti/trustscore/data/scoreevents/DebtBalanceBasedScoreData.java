package io.coti.trustscore.data.scoreevents;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertDebtBalanceBasedScoreRequest;
import lombok.Data;

@Data
public class DebtBalanceBasedScoreData extends BalanceBasedScoreData {

    private static final long serialVersionUID = 189694756168266770L;
    private Hash plaintiffUserHash;

    public DebtBalanceBasedScoreData() {
    }

    public DebtBalanceBasedScoreData(InsertDebtBalanceBasedScoreRequest request) {
        super(request, FinalScoreType.DEBT);
        this.plaintiffUserHash = request.getOtherUserHash();
        this.amount = request.getAmount();
    }
}