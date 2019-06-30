package io.coti.historynode.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;

@Data
public class AddressTransactionsByDate implements IEntity {

    private Instant date;
    private HashSet<Hash> transactionsAddresses;

    public AddressTransactionsByDate(Instant date, HashSet<Hash> transactionsAddresses) {
        this.date = date;
        this.transactionsAddresses = transactionsAddresses;
    }

    @Override
    public Hash getHash() {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        LocalDate localDate = LocalDate.of(ldt.getYear(), ldt.getMonth(),ldt.getDayOfMonth());
        return CryptoHelper.cryptoHash(localDate.atStartOfDay().toString().getBytes());
    }

    @Override
    public void setHash(Hash hash) {
    }

}
