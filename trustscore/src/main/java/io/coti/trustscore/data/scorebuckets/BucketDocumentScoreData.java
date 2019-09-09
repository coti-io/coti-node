package io.coti.trustscore.data.scorebuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.scoreevents.DocumentScoreData;
import io.coti.trustscore.data.parameters.DocumentDecayedContributionData;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketDocumentScoreData extends BucketData<DocumentScoreData> implements IEntity {

    private static final long serialVersionUID = -8539837840657593405L;
    private Map<FinalScoreType, DocumentDecayedContributionData> actualScoresDataMap;

    public BucketDocumentScoreData() {
        super();
        actualScoresDataMap = new ConcurrentHashMap<>();
    }

}
