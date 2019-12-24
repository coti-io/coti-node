package io.coti.trustscore.data.tsbuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.tsenums.FinalEventType;
import io.coti.trustscore.data.tsevents.DocumentEventData;
import io.coti.trustscore.data.contributiondata.DocumentDecayedContributionData;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketDocumentEventData extends BucketData<DocumentEventData> implements IEntity {

    private static final long serialVersionUID = -8539837840657593405L;
    private Map<FinalEventType, DocumentDecayedContributionData> actualScoresDataMap;

    public BucketDocumentEventData() {
        super();
        actualScoresDataMap = new ConcurrentHashMap<>();
    }

}
