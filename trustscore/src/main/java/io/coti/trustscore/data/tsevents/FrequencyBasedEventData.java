package io.coti.trustscore.data.tsevents;

import io.coti.trustscore.http.InsertChargeBackFrequencyBasedScoreRequest;
import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

@Data
public abstract class FrequencyBasedEventData extends SignedEventData {
    private static final long serialVersionUID = 6122309298923683652L;

    public FrequencyBasedEventData() {
    }

    // Been created using reflection
    public FrequencyBasedEventData(InsertEventScoreRequest request) {
        super(request, request.getEventIdentifier());
    }

    public FrequencyBasedEventData(InsertChargeBackFrequencyBasedScoreRequest request) {
        super(request, request.getEventIdentifier());
    }
}
