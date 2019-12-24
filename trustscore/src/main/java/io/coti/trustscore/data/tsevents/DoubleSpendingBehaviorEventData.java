package io.coti.trustscore.data.tsevents;


import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

@Data
public class DoubleSpendingBehaviorEventData extends BehaviorEventData {

    private static final long serialVersionUID = 4700090327896274767L;

    public DoubleSpendingBehaviorEventData() {
    }

// Been created using reflection
    public DoubleSpendingBehaviorEventData(InsertEventScoreRequest request) {
        super(request);
    }
}