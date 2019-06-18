package io.coti.historynode.services;


import io.coti.basenode.data.TransactionData;
import io.coti.historynode.data.AddressTransactionsByDatesHistory;
import io.coti.historynode.model.AddressTransactionsByDatesHistories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AddressTransactionsService implements io.coti.historynode.services.interfaces.IAddressTransactionsService {

    @Autowired
    private AddressTransactionsByDatesHistories addressTransactionsByDatesHistories;

    @Override
    public void saveToAddressTransactionsHistories(TransactionData transaction) {
        AddressTransactionsByDatesHistory addressTransactionsByDatesHistory =
                addressTransactionsByDatesHistories.getByHash(transaction.getSenderHash());
        if (addressTransactionsByDatesHistory == null) {
            addressTransactionsByDatesHistory = new AddressTransactionsByDatesHistory(transaction.getSenderHash());
        }
        addressTransactionsByDatesHistory.getTransactionsHistory().put(transaction.getAttachmentTime().toEpochMilli(), transaction.getSenderHash());
        addressTransactionsByDatesHistories.put(addressTransactionsByDatesHistory);
    }
}
