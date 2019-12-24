package io.coti.trustscore.data.tsevents;

import io.coti.trustscore.http.InsertDocumentScoreRequest;
import lombok.Data;

@Data
public class Questionnaire2DocumentEventData extends DocumentEventData {

    private static final long serialVersionUID = 8903254773832568402L;

    public Questionnaire2DocumentEventData() {
    }

// Been created using reflection
    public Questionnaire2DocumentEventData(InsertDocumentScoreRequest request) {
        super(request);
        this.score = request.getScore();
    }

}
