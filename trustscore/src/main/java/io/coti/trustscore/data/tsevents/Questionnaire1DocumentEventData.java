package io.coti.trustscore.data.tsevents;

import io.coti.trustscore.http.InsertDocumentScoreRequest;
import lombok.Data;

@Data
public class Questionnaire1DocumentEventData extends DocumentEventData {

    private static final long serialVersionUID = -8586714818256567189L;

    public Questionnaire1DocumentEventData() {
    }

// Been created using reflection
    public Questionnaire1DocumentEventData(InsertDocumentScoreRequest request) {
        super(request);
        this.score = request.getScore();
    }
}
