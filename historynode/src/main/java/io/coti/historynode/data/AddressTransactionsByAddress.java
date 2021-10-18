package io.coti.historynode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;

@Data
public class AddressTransactionsByAddress implements IEntity {

    private static final long serialVersionUID = -2778931749700204850L;
    private Hash address;
    private Map<LocalDate, HashSet<Hash>> transactionHashesByDates;
    private LocalDate startDate;

    public AddressTransactionsByAddress(Hash address, Map<LocalDate, HashSet<Hash>> transactionHashesByDate, LocalDate startDate) {
        this.address = address;
        this.transactionHashesByDates = transactionHashesByDate;
        this.startDate = startDate;
    }

    @Override
    public Hash getHash() {
        return address;
    }

    @Override
    public void setHash(Hash hash) {
        this.address = hash;
    }
}
