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

    private Hash hash;
    private Instant date;
    private HashSet<Hash> transactionHashes;

    public AddressTransactionsByDate(Instant date, HashSet<Hash> transactionHashes) {
        this.date = date;
        this.transactionHashes = transactionHashes;
        initHashByDate();
    }

    private void initHashByDate() {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        LocalDate localDate = LocalDate.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth());
        this.hash = CryptoHelper.cryptoHash(localDate.toString().getBytes());
    }

}
