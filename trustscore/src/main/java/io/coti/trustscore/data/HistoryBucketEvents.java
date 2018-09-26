package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;

import java.util.List;
import java.util.Map;

public class HistoryBucketEvents{

    private Hash userHash;
    private Map<EventType,List<BucketEventData>> historyBucketsEvents;
}
