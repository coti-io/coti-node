package io.coti.trustscore.data.tsevents;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.http.InsertDebtBalanceBasedScoreRequest;
import lombok.Data;

@Data
public class DebtBalanceBasedEventData extends BalanceBasedEventData {

    private static final long serialVersionUID = 189694756168266770L;
    private Hash plaintiffUserHash;

    public DebtBalanceBasedEventData() {
    }

// Been created using reflection
    public DebtBalanceBasedEventData(InsertDebtBalanceBasedScoreRequest request) {
        super(request);
        this.plaintiffUserHash = request.getOtherUserHash();
        this.amount = request.getAmount();
    }
}