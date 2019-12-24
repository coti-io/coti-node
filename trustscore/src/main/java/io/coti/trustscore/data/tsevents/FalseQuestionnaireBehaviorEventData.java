package io.coti.trustscore.data.tsevents;


import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

@Data
public class FalseQuestionnaireBehaviorEventData extends BehaviorEventData {

    private static final long serialVersionUID = 3576212918648240934L;

    public FalseQuestionnaireBehaviorEventData() {
    }

    // Been created using reflection
    public FalseQuestionnaireBehaviorEventData(InsertEventScoreRequest request) {
        super(request);
    }
}