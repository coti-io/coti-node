package io.coti.trustscore.data;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.utils.BucketBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Data
@Slf4j
public class TrustScoreUserData implements IEntity {

    private Hash userHash;
    private double initialTS;
    private double currentTS;
    private Date calculatedTsDateTime;
    private UserType userType;
    private HashMap<EventType,BucketEventData> lastBucketEventData;
    private HashMap<EventType, List<Double>> bucketsHistoryCalculations;

    private TrustScoreUserData(){};

    public TrustScoreUserData(Hash userHash, UserType userType){
        init();
        this.userType = userType;
        this.userHash = userHash;
    }


    private void init(){

        this.bucketsHistoryCalculations= new HashMap<>();
        this.lastBucketEventData= new HashMap<>();
        this.lastBucketEventData.put(EventType.TRANSACTION, new BucketTransactionEventsData());
        this.lastBucketEventData.put(EventType.DISPUTE, new BucketDisputeEventsData());
    }

    public double getCurrentTs(){
        if (shouldRecalculate())
            calculateTsOnNewDate();
        return currentTS;
    }



    private boolean shouldRecalculate(){

        //TODO: need to change here to check date
        return true;
    }

    private void calculateTsOnNewDate(){
        DoShiftToBuckets();
        double currentTsValue = initialTS;
        for (Map.Entry<EventType,BucketEventData> entry : lastBucketEventData.entrySet()) {
            currentTsValue += calculateBucketTypeMagnitude(bucketsHistoryCalculations.get(entry.getKey())) + entry.getValue().CalculatedDelta;
        }
        currentTS = currentTsValue;
    }



    public void addEvent(EventData event){

        if (!lastBucketEventData.containsKey(event.eventType)) {
            try {
                lastBucketEventData.put(event.eventType, BucketBuilder.CreateBucket(event.eventType));
            } catch (IllegalAccessException | InstantiationException e) {
                log.error("error while trying create a bucket", e);
            }
        }
        BucketEventData lastBucket = lastBucketEventData.get(event.eventType);
        lastBucket.addEventToBucket(event);
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

    private double calculateBucketTypeMagnitude(List<Double> listOfEvents){
        int length = listOfEvents.size();
        double sumValue = listOfEvents.stream().mapToDouble(Double::doubleValue).sum();
        return sumValue / length;
    }

    public void DoShiftToBuckets(){
        lastBucketEventData.values().stream().forEach(bucket-> bucket.ShiftCalculatedTsContribution());
    }
}


