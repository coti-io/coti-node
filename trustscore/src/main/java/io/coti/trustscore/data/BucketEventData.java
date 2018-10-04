package io.coti.trustscore.data;
import io.coti.basenode.data.Hash;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public abstract class BucketEventData<T extends EventData> implements Serializable {

    protected Date StartPeriodTime;
    protected double CalculatedDelta;
    protected Date LastDateCalculated;

    protected abstract double getWeight();
    protected abstract double getDecay();
    protected abstract int bucketPeriodTime();



    Map<Hash, EventData> bucketEvents;



    public BucketEventData(){
        bucketEvents = new LinkedHashMap<>();
        StartPeriodTime = new Date();
        CalculatedDelta = 0;
    }

    public void addEventToBucket(T eventData){
        if (bucketEvents.containsKey(eventData.getHash())) return;


        //TODO: when adding check if last added date0 is as current
        addEventToCalculations(eventData);


        //TODO: if we have a problem here, event can be added without calculated
        bucketEvents.put(eventData.getUniqueIdentifier(),eventData);


    }

    protected abstract void addEventToCalculations(T eventData);

    public double getBucketEventAddition(){
        return CalculatedDelta;
    }


    public abstract void ShiftCalculatedTsContribution();
}


