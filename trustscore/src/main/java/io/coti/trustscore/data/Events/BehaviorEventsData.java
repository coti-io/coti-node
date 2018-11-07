package io.coti.trustscore.data.Events;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.data.Enums.BehaviorEventsScoreType;
import lombok.Data;

@Data
public class BehaviorEventsData extends EventData {
    private TransactionData transactionData;
    private SignatureData eventSignature;
    private BehaviorEventsScoreType behaviorEventsScoreType;

    public BehaviorEventsData(BehaviorEventsScoreType behaviorEventsScoreType, SignatureData eventSignature) {
        this.behaviorEventsScoreType = behaviorEventsScoreType;
        this.eventSignature = eventSignature;
    }

    public BehaviorEventsData(BehaviorEventsScoreType behaviorEventsScoreType, SignatureData eventSignature, TransactionData transactionData) {
        this(behaviorEventsScoreType, eventSignature);
        this.transactionData = transactionData;
    }

    @Override
    public int hashCode() {
        if (transactionData != null) {
            return transactionData.getHash().hashCode();
        }

        return super.hashCode();
    }

    @Override
    public Hash getHash() {
        if (transactionData != null) {
            return this.transactionData.getHash();
        }
        return super.getHash();
    }

    @Override
    public void setHash(Hash hash) {
        if (transactionData != null) {
            this.transactionData.setHash(hash);
        }

        super.setHash(hash);
    }
}