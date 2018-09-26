package io.coti.trustscore.data;
import io.coti.basenode.data.Hash;
import lombok.Data;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public abstract class BucketEventData<T extends EventData>  {

    protected Date StartPeriodTime;
    protected double CalculatedDelta;
    protected Date LastDateCalculated;
    protected abstract int bucketPeriodTime();



    Map<Hash, EventData> bucketEvents;



    public BucketEventData(){
        bucketEvents = new LinkedHashMap<>();
        StartPeriodTime = new Date();
        CalculatedDelta = 0;
    }

    public void addEventToBucket(T eventData){
        if (bucketEvents.containsKey(eventData.getUniqueIdentifier())) return;

        //TODO:
        bucketEvents.put(eventData.getUniqueIdentifier(),eventData);

        addEventToCalculations(eventData);
        //TODO: when adding check if last added date0 is as current
    }

    protected abstract void addEventToCalculations(T eventData);

    public double getBucketEventAddition(){
        return CalculatedDelta;
    }

}


