package io.coti.trustscore.data.tsevents;

import io.coti.trustscore.http.InsertDocumentScoreRequest;
import lombok.Data;

@Data
public class Questionnaire3DocumentEventData extends DocumentEventData {

    private static final long serialVersionUID = 2750243157421392107L;

    public Questionnaire3DocumentEventData() {
    }

// Been created using reflection
    public Questionnaire3DocumentEventData(InsertDocumentScoreRequest request) {
        super(request);
        this.score = request.getScore();
    }

}
