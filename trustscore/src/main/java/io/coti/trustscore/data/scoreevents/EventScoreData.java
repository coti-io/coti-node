package io.coti.trustscore.data.scoreevents;


import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

@Data
public abstract class EventScoreData extends SignedScoreData {

    private static final long serialVersionUID = 5474716203264386699L;

    public EventScoreData() {
    }

    public EventScoreData(InsertEventScoreRequest request, FinalScoreType finalScoreType) {
        super(request, finalScoreType, request.eventIdentifier);
    }
}