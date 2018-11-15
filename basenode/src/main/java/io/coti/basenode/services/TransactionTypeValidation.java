package io.coti.basenode.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.OutputBaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.interfaces.ITransactionTypeValidation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public enum TransactionTypeValidation implements ITransactionTypeValidation {
    Payment(TransactionType.Payment) {

    },
    Transfer(TransactionType.Transfer) {

    };
    private TransactionType type;

    TransactionTypeValidation(TransactionType type) {
        this.type = type;
    }

    @Override
    public boolean validateOutputBaseTransactions(TransactionData transactionData) {
        List<OutputBaseTransactionData> outputBaseTransactions = transactionData.getOutputBaseTransactions();
        return true;
    }

}
