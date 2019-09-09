package io.coti.trustscore.data.scoreevents;

import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertChargeBackFrequencyBasedScoreRequest;
import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

@Data
public abstract class FrequencyBasedScoreData extends SignedScoreData {

    private static final long serialVersionUID = 6122309298923683652L;

    public FrequencyBasedScoreData() {
    }

    public FrequencyBasedScoreData(InsertEventScoreRequest request, FinalScoreType finalScoreType) {
        super(request, finalScoreType, request.eventIdentifier);
    }

    public FrequencyBasedScoreData(InsertChargeBackFrequencyBasedScoreRequest request, FinalScoreType finalScoreType) {
        super(request, finalScoreType, request.eventIdentifier);
    }
}
