package io.coti.trustscore.data.scoreevents;


import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Data
public class FillQuestionnaireEventScoreData extends EventScoreData {

    private static final long serialVersionUID = 243956068029213157L;

    public FillQuestionnaireEventScoreData() {
    }

    public FillQuestionnaireEventScoreData(InsertEventScoreRequest request) {
        super(request, FinalScoreType.FILLQUESTIONNAIRE);
    }

    public FillQuestionnaireEventScoreData(Hash eventIdentifier) {
        this.setEventDate(LocalDate.now(ZoneOffset.UTC));
        this.setHash(eventIdentifier);
    }
}