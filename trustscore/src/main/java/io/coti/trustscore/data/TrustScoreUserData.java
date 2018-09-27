package io.coti.trustscore.data;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.*;

@Data
public class TrustScoreUserData implements IEntity {

    private Hash userHash;
    private double initialTS;
    private double currentTS;
    private Date calculatedTsDateTime;

    private Map<EventType,BucketEventData> lastBucketEventData;
    private Map<EventType, Map<Hash,EventData>> userEvents;


    public TrustScoreUserData(){
        lastBucketEventData.put(EventType.TRANSACTION, new BucketTransactionEventsData());
    }


    private void calculateTsOnNewDate(){
    }

    public void addTransactionEvent(EventData eventData){

        BucketEventData lastBucket = lastBucketEventData.get(EventType.TRANSACTION);
        lastBucket.addEventToBucket(eventData);
    }

    public void addEvent(EventData event){
        if (!userEvents.containsKey(event.eventType))
            userEvents.put(event.eventType, new HashMap<>());

        Map<Hash,EventData> badTypeEvents = userEvents.get(event.eventType);
        if (userEvents.get(event.eventType).containsKey(event.getHash())) return;
        badTypeEvents.put(event.getHash(), event);
    }

    private void removeEvents(){
    }

    @Override
    public Hash getHash() {
        return this.userHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.userHash = hash;
    }

    public void DoShiftToBuckets(){

    }
}
