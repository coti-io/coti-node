package io.coti.historynode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;

@Data
public class AddressTransactionsByAddress implements IEntity {

    private Hash address;
    private HashMap<LocalDate, HashSet<Hash>> transactionHashesByDates;
    private Instant startDate;

    public AddressTransactionsByAddress(Hash address, HashMap<LocalDate, HashSet<Hash>> transactionHashesByDate, Instant startDate) {
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

    }
}
