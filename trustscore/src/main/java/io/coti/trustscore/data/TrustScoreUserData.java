package io.coti.trustscore.data;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import java.util.Date;
import java.util.Map;

@Data
public class TrustScoreUserData implements IEntity {

    private Hash userHash;
    private double initialTS;
    private double currentTS;
    private Date calculatedTsDateTime;

    private Map<EventType,BucketEventData> lastBucketEventData;

    private void calculateTsOnNewDate(){
    }

    public void addTransactionEvent(TransactionData transactionData){

        BucketTransactionEventsData lastBucket = (BucketTransactionEventsData)lastBucketEventData.get(EventType.TRANSACTION);
        lastBucket.addEventToBucket(new TransactionEventData(transactionData, lastBucket.getLastNumberOfTransactions(), lastBucket.getLastTurnOver(),lastBucket.getLastBalance()));
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
}
