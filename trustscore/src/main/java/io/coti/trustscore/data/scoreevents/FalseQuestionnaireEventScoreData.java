package io.coti.trustscore.data.scoreevents;


import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.scoreenums.UserType;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.http.InsertEventScoreRequest;
import lombok.Data;

import java.util.Map;

@Data
public class FalseQuestionnaireEventScoreData extends EventScoreData {

    private static final long serialVersionUID = 3576212918648240934L;
    public static Map<UserType, UserParameters> userParametersMap;

    public FalseQuestionnaireEventScoreData() {
    }

    public FalseQuestionnaireEventScoreData(InsertEventScoreRequest request) {
        super(request, FinalScoreType.FALSEQUESTIONNAIRE);
    }
}