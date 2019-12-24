package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class InsertDocumentScoreResponse extends BaseResponse {
    private String userHash;
    private String documentType;
    private double score;

    public InsertDocumentScoreResponse(String userHash, String documentType, double score) {
        super();
        this.userHash = userHash;
        this.score = score;
        this.documentType = documentType;

    }
}
