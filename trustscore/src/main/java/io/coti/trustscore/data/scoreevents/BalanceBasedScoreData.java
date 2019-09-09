package io.coti.trustscore.data.scoreevents;

import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.SignedRequest;
import lombok.Data;

import java.math.BigDecimal;

@Data
public abstract class BalanceBasedScoreData extends SignedScoreData {

    private static final long serialVersionUID = -160695542578319293L;
    protected BigDecimal amount;

    public BalanceBasedScoreData() {
    }

    public BalanceBasedScoreData(SignedRequest request, FinalScoreType finalScoreType) {
        super(request, finalScoreType);
    }
}