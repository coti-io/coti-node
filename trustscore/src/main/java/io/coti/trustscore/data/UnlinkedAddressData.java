package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.scoreevents.TransactionScoreData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Data
public class UnlinkedAddressData implements IEntity {

    private static final long serialVersionUID = -7404291182897047913L;
    private Hash address;
    private ConcurrentSkipListMap<LocalDate, Double> dateToBalanceMap;
    private boolean zeroTrustFlag;

    public UnlinkedAddressData(Hash address) {
        this.address = address;
        this.dateToBalanceMap = new ConcurrentSkipListMap<>();
        this.zeroTrustFlag = false;
    }

    public void insertToDateToBalanceMap(TransactionScoreData transactionEventData, BigDecimal amount) {
        LocalDate transactionDate = transactionEventData.getEventDate();
        LocalDate nextDate = transactionDate.plusDays(1);

        if (!dateToBalanceMap.isEmpty() && dateToBalanceMap.containsKey(transactionDate)) {
            dateToBalanceMap.put(transactionDate, dateToBalanceMap.get(transactionDate) + amount.doubleValue());
        } else {
            ConcurrentSkipListMap.Entry<LocalDate, Double> previous;
            previous = dateToBalanceMap.lowerEntry(transactionDate);
            if (previous != null) {
                dateToBalanceMap.put(transactionDate, previous.getValue() + amount.doubleValue());
            } else {
                dateToBalanceMap.put(transactionDate, amount.doubleValue());
            }
        }
        if (!dateToBalanceMap.isEmpty() && dateToBalanceMap.containsKey(nextDate)) {
            dateToBalanceMap.put(nextDate, dateToBalanceMap.get(nextDate) + amount.doubleValue());
        }
    }

    public void insertToDateToBalanceMap(LocalDate transactionDate, double amount) {
        LocalDate nextDate = transactionDate.plusDays(1);

        if (!dateToBalanceMap.isEmpty() && dateToBalanceMap.containsKey(transactionDate)) {
            dateToBalanceMap.put(transactionDate, dateToBalanceMap.get(transactionDate) + amount);
        } else {
            dateToBalanceMap.put(transactionDate, amount);
        }
        if (!dateToBalanceMap.isEmpty() && dateToBalanceMap.containsKey(nextDate)) {
            dateToBalanceMap.put(nextDate, dateToBalanceMap.get(nextDate) + amount);
        }
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
