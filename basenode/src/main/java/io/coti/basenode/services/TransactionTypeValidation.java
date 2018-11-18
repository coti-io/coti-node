package io.coti.basenode.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.OutputBaseTransactionName;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.interfaces.ITransactionTypeValidation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public enum TransactionTypeValidation implements ITransactionTypeValidation {
    Payment(TransactionType.Payment),
    Transfer(TransactionType.Transfer),
    ZeroSpend(TransactionType.ZeroSpend) {
        @Override
        public boolean validateInputBaseTransactions(TransactionData transactionData) {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException("Invalid transaction type");
            }
            List<BaseTransactionData> inputBaseTransactions = transactionData.getInputBaseTransactions();
            return inputBaseTransactions.size() == 1 && inputBaseTransactions.get(0).getAmount().equals(BigDecimal.ZERO);
        }
    };

    protected TransactionType type;
    protected final String packagePath = "io.coti.basenode.data.";

    TransactionTypeValidation(TransactionType type) {
        this.type = type;
    }

    @Override
    public boolean validateBaseTransactions(TransactionData transactionData) {
        return validateInputBaseTransactions(transactionData) && validateOutputBaseTransactions(transactionData);
    }

    @Override
    public boolean validateInputBaseTransactions(TransactionData transactionData) {
        if (!type.equals(transactionData.getType())) {
            throw new IllegalArgumentException("Invalid transaction type");
        }
        return true;
    }
    @Override
    public boolean validateOutputBaseTransactions(TransactionData transactionData) {
        try {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException("Invalid transaction type");
            }

            List<BaseTransactionData> outputBaseTransactions = transactionData.getOutputBaseTransactions();
            List<String> outputBaseTransactionNames = OutputBaseTransactionName.getOutputBaseTransactionsByType(type);
            if (outputBaseTransactionNames.size() != outputBaseTransactions.size()) {
                return false;
            }
            for (int i = 0; i < outputBaseTransactions.size(); i++) {
                if (!Class.forName(outputBaseTransactionNames.get(i)).equals(outputBaseTransactions.get(i))) {
                    return false;
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

}
