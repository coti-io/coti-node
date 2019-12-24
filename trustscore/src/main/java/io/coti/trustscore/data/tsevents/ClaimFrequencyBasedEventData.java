package io.coti.trustscore.data.tsevents;

import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

@Data
public class ClaimFrequencyBasedEventData extends FrequencyBasedEventData {
    private static final long serialVersionUID = 5856910049512299462L;

    public ClaimFrequencyBasedEventData() {
    }

    // Been created using reflection
    public ClaimFrequencyBasedEventData(InsertEventScoreRequest request) {
        super(request);
    }
}
