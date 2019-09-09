package io.coti.trustscore.data.scoreevents;

import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertDocumentScoreRequest;
import lombok.Data;

@Data
public class Questionnaire1DocumentScoreData extends DocumentScoreData {

    private static final long serialVersionUID = -8586714818256567189L;

    public Questionnaire1DocumentScoreData() {
    }

    public Questionnaire1DocumentScoreData(InsertDocumentScoreRequest request) {
        super(request, FinalScoreType.QUESTIONNAIRE1);
        this.score = request.getScore();
    }
}
