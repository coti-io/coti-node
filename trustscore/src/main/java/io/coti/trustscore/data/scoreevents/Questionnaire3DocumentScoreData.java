package io.coti.trustscore.data.scoreevents;

import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertDocumentScoreRequest;
import lombok.Data;

@Data
public class Questionnaire3DocumentScoreData extends DocumentScoreData {

    private static final long serialVersionUID = 2750243157421392107L;

    public Questionnaire3DocumentScoreData() {
    }

    public Questionnaire3DocumentScoreData(InsertDocumentScoreRequest request) {
        super(request, FinalScoreType.QUESTIONNAIRE3);
        this.score = request.getScore();
    }

}
