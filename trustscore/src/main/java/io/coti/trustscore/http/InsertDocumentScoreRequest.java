package io.coti.trustscore.http;

import io.coti.trustscore.data.scoreenums.DocumentRequestType;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InsertDocumentScoreRequest extends SignedRequest {
    private static final long serialVersionUID = 9154754970965594479L;
    @NotNull
    public DocumentRequestType documentType;
    @NotNull
    public double score;
}