package io.coti.trustscore.data.scoreevents;

import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertDocumentScoreRequest;
import io.coti.trustscore.http.SetKycTrustScoreRequest;
import lombok.Data;

@Data
public class KYCDocumentScoreData extends DocumentScoreData {

    private static final long serialVersionUID = -3207820319956741493L;

    public KYCDocumentScoreData() {
    }

    public KYCDocumentScoreData(SetKycTrustScoreRequest request) {
        super(request, FinalScoreType.KYC);
        this.score = request.getKycTrustScore() - 10.0;
    }

    public KYCDocumentScoreData(InsertDocumentScoreRequest request) {
        super(request, FinalScoreType.KYC);
        this.score = request.getScore() - 10.0;
    }
}
