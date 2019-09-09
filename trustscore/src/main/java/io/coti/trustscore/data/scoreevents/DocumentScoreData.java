package io.coti.trustscore.data.scoreevents;

import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.SignedRequest;
import lombok.Data;


@Data
public abstract class DocumentScoreData extends SignedScoreData {

    private static final long serialVersionUID = 9074125393939297193L;
    protected double score;

    public DocumentScoreData() {
    }

    public DocumentScoreData(SignedRequest request, FinalScoreType finalScoreType) {
        super(request, finalScoreType);
    }

}
