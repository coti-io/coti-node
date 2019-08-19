package io.coti.basenode.data;


import com.google.common.collect.Sets;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class AddressTransactionsHistory implements IEntity {

    private static final long serialVersionUID = 6148574209385536578L;
    private transient Hash hash;
    private Instant creationTime;
    private Set<Hash> transactionsHistory;

    public AddressTransactionsHistory(Hash hash) {
        this.hash = hash;
        creationTime = Instant.now();
        transactionsHistory = Sets.newConcurrentHashSet();
    }

    public boolean addTransactionHashToHistory(Hash transactionHash) {
        return transactionsHistory.add(transactionHash);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddressTransactionsHistory)) {
            return false;
        }
        return hash.equals(((AddressTransactionsHistory) other).hash);
    }
}



