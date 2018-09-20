package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import javafx.util.Pair;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class UserCurrentTsData implements IEntity {
    private Hash userHash;
    private double initialTS;
    private double currentTS;
    private Date calculatedTsDateTime;
    private Map<EventType,Double> decayedEventsTs;
    private Map<EventType, Pair<List<EventData>, Double>> todayEventTs;
    private double decayedTransactionTs;
    private Pair<List<TransactionTsData>, Double> todayTransactionsTs;


    @Override
    public Hash getHash() {
        return this.userHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.userHash = hash;
    }
}
