package io.coti.historynode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.SortedMap;
import java.util.TreeMap;

@Data
public class AddressTransactionsByDatesHistory implements IEntity {
    private transient Hash hash;
    private SortedMap<Long, Hash> transactionsHistory;

    public AddressTransactionsByDatesHistory(Hash hash) {
        this.hash = hash;
        transactionsHistory = new TreeMap();
    }


    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof io.coti.historynode.data.AddressTransactionsByDatesHistory)) {
            return false;
        }
        return hash.equals(((io.coti.historynode.data.AddressTransactionsByDatesHistory) other).hash);
    }
}
