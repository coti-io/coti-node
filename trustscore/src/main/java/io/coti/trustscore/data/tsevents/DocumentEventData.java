package io.coti.trustscore.data.tsevents;

import io.coti.trustscore.http.SignedRequest;
import lombok.Data;


@Data
public abstract class DocumentEventData extends SignedEventData {
    private static final long serialVersionUID = 9074125393939297193L;
    protected double score;

    public DocumentEventData() {
    }

// Been created using reflection
    public DocumentEventData(SignedRequest request) {
        super(request);
    }

}
