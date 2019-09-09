package io.coti.trustscore.data.scoreevents;


import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

@Data
public class DoubleSpendingEventScoreData extends EventScoreData {

    private static final long serialVersionUID = 4700090327896274767L;

    public DoubleSpendingEventScoreData() {
    }

    public DoubleSpendingEventScoreData(InsertEventScoreRequest request) {
        super(request, FinalScoreType.DOUBLESPENDING);
    }
}