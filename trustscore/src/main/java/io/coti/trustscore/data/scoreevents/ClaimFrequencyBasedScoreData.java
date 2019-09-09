package io.coti.trustscore.data.scoreevents;

import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

@Data
public class ClaimFrequencyBasedScoreData extends FrequencyBasedScoreData {

    private static final long serialVersionUID = 5856910049512299462L;

    public ClaimFrequencyBasedScoreData() {
    }

    public ClaimFrequencyBasedScoreData(InsertEventScoreRequest request) {
        super(request, FinalScoreType.CLAIM);
    }
}
