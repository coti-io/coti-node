package io.coti.trustscore.data.scoreevents;

import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertDocumentScoreRequest;
import lombok.Data;

@Data
public class Questionnaire2DocumentScoreData extends DocumentScoreData {

    private static final long serialVersionUID = 8903254773832568402L;

    public Questionnaire2DocumentScoreData() {
    }

    public Questionnaire2DocumentScoreData(InsertDocumentScoreRequest request) {
        super(request, FinalScoreType.QUESTIONNAIRE2);
        this.score = request.getScore();
    }

}
