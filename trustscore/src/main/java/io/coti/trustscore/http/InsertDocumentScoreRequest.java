package io.coti.trustscore.http;

import io.coti.trustscore.data.tsenums.DocumentRequestType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InsertDocumentScoreRequest extends SignedRequest {
    @NotNull
    private DocumentRequestType documentType;
    @NotNull
    private double score;
}