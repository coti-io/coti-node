package io.coti.fullnode.data;

import com.google.common.collect.Sets;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.Instant;
import java.util.Arrays;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

@Data
public class AddressTransactionsByAttachment implements IEntity {

    private static final long serialVersionUID = 4868526205174968473L;
    private Hash hash;
    private Instant creationTime;
    private NavigableMap<Instant, Set<Hash>> transactionsHistoryByAttachment;

    public AddressTransactionsByAttachment(Hash hash) {
        this.hash = hash;
        creationTime = Instant.now();
        transactionsHistoryByAttachment = new TreeMap<>();
    }

    public boolean addTransactionHashToHistory(Hash transactionHash, Instant attachmentTime) {
        Set<Hash> transactionsHistory = transactionsHistoryByAttachment.get(attachmentTime);
        if (transactionsHistory != null) {
            return transactionsHistory.add(transactionHash);
        }
        transactionsHistory = Sets.newConcurrentHashSet();
        transactionsHistory.add(transactionHash);
        transactionsHistoryByAttachment.put(attachmentTime, transactionsHistory);
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddressTransactionsByAttachment)) {
            return false;
        }
        return hash.equals(((AddressTransactionsByAttachment) other).hash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash.getBytes());
    }
}
