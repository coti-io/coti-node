package io.coti.fullnode.data;

import com.google.common.collect.Sets;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

@Data
public class DateAddressTransactionsHistory implements IEntity {
    private static final long serialVersionUID = 6148574209385536578L;
    private Hash hash;
    private Instant creationTime;
    private NavigableMap<LocalDate, Set<Hash>> transactionsHistoryByDate;

    public DateAddressTransactionsHistory(Hash hash) {
        this.hash = hash;
        creationTime = Instant.now();
        transactionsHistoryByDate = new TreeMap<LocalDate, Set<Hash>>();
    }

    public boolean addTransactionHashByDateToHistory(Hash transactionHash, LocalDate localDate) {
        Set<Hash> transactionsHistory = transactionsHistoryByDate.get(localDate);
        if( transactionsHistory != null){
            return transactionsHistory.add(transactionHash);
        }
        transactionsHistory = Sets.newConcurrentHashSet();
        transactionsHistory.add(transactionHash);
        transactionsHistoryByDate.put(localDate, transactionsHistory);
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof DateAddressTransactionsHistory)) {
            return false;
        }
        return hash.equals(((DateAddressTransactionsHistory) other).hash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash.getBytes());
    }
}
