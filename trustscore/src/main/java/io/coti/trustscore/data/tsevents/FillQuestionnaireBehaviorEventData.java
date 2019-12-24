package io.coti.trustscore.data.tsevents;


import io.coti.basenode.data.Hash;
import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Data
public class FillQuestionnaireBehaviorEventData extends BehaviorEventData {

    private static final long serialVersionUID = 243956068029213157L;

    public FillQuestionnaireBehaviorEventData() {
    }

    // Been created using reflection
    public FillQuestionnaireBehaviorEventData(InsertEventScoreRequest request) {
        super(request);
    }

    public FillQuestionnaireBehaviorEventData(Hash eventIdentifier) {
        this.setEventDate(LocalDate.now(ZoneOffset.UTC));
        this.setHash(eventIdentifier);
    }
}