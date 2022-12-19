package io.coti.historynode.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@Data
public class AddressTransactionsByDate implements IEntity {

    private static final long serialVersionUID = 4576893520654304534L;
    private Hash hash;
    private Instant date;
    private Set<Hash> transactionHashes;

    public AddressTransactionsByDate(Instant date, Set<Hash> transactionHashes) {
        this.date = date;
        this.transactionHashes = transactionHashes;
        initHashByDate();
    }

    private void initHashByDate() {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        LocalDate localDate = LocalDate.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth());
        this.hash = CryptoHelper.cryptoHash(localDate.toString().getBytes(StandardCharsets.UTF_8));
    }

}
