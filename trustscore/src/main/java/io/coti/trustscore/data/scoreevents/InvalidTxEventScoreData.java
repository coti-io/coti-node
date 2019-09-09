package io.coti.trustscore.data.scoreevents;


import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

@Data
public class InvalidTxEventScoreData extends EventScoreData {

    private static final long serialVersionUID = -5864778424517102382L;

    public InvalidTxEventScoreData() {
    }

    public InvalidTxEventScoreData(InsertEventScoreRequest request) {
        super(request, FinalScoreType.INVALIDTX);
    }
}