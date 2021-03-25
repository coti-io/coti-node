package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionHelper;
import io.coti.fullnode.data.DateAddressTransactionsHistory;
import io.coti.fullnode.model.DateAddressTransactionsHistories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TransactionHelper extends BaseNodeTransactionHelper {
    @Autowired
    private DateAddressTransactionsHistories dateAddressTransactionsHistories;

    @Override
    public void updateAddressTransactionHistory(TransactionData transactionData) {
        super.updateAddressTransactionHistory(transactionData);

        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            DateAddressTransactionsHistory dateAddressTransactionsHistory = Optional.ofNullable(dateAddressTransactionsHistories.getByHash(baseTransactionData.getAddressHash()))
                    .orElse(new DateAddressTransactionsHistory(baseTransactionData.getAddressHash()));
            LocalDate localDate = baseTransactionData.getCreateTime().atZone(ZoneId.of("UTC")).toLocalDate();
            if (!dateAddressTransactionsHistory.addTransactionHashByDateToHistory(transactionData.getHash(), localDate)) {
                log.debug("Transaction {} in date {} is already in history of address {}", transactionData.getHash(), localDate, baseTransactionData.getAddressHash());
            }
            dateAddressTransactionsHistories.put(dateAddressTransactionsHistory);
        });
    }

    public void updateDateAddressTransactionHistory(Map<Hash, DateAddressTransactionsHistory> dateAddressToTransactionsHistoryMap, TransactionData transactionData) {
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            DateAddressTransactionsHistory dateAddressHistory;
            if (!dateAddressToTransactionsHistoryMap.containsKey(baseTransactionData.getAddressHash())) {
                dateAddressHistory = dateAddressTransactionsHistories.getByHash(baseTransactionData.getAddressHash());
                if (dateAddressHistory == null) {
                    dateAddressHistory = new DateAddressTransactionsHistory(baseTransactionData.getAddressHash());
                }
            } else {
                dateAddressHistory = dateAddressToTransactionsHistoryMap.get(baseTransactionData.getAddressHash());
            }
            LocalDate localDate = LocalDate.from(baseTransactionData.getCreateTime());
            if (!dateAddressHistory.addTransactionHashByDateToHistory(transactionData.getHash(), localDate) ) {
                log.debug("Transaction {} is already in history of address {} local date {}", transactionData.getHash(), baseTransactionData.getAddressHash(), localDate);
            }
            dateAddressToTransactionsHistoryMap.put(baseTransactionData.getAddressHash(), dateAddressHistory);
        });
    }

}
