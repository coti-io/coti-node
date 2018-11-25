package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.ITransactionTypeValidation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public enum TransactionTypeValidation implements ITransactionTypeValidation {
    Payment(TransactionType.Payment),
    Transfer(TransactionType.Transfer) {
        @Override
        public boolean validateReducedAmount(List<OutputBaseTransactionData> outputBaseTransactions) {
            BigDecimal reducedAmount = BigDecimal.ZERO;
            BigDecimal networkFeeAmount = BigDecimal.ZERO;

            for (OutputBaseTransactionData outputBaseTransactionData : outputBaseTransactions) {
                if (NetworkFeeData.class.isInstance(outputBaseTransactionData)) {
                    reducedAmount = ((NetworkFeeData) outputBaseTransactionData).getReducedAmount();
                    networkFeeAmount = outputBaseTransactionData.getAmount();
                } else if(ReceiverBaseTransactionData.class.isInstance(outputBaseTransactionData)) {
                    return outputBaseTransactionData.getAmount().equals(reducedAmount.add(networkFeeAmount));
                }
            }
            return false;
        }
    },
    ZeroSpend(TransactionType.ZeroSpend) {
        @Override
        public boolean validateInputBaseTransactions(TransactionData transactionData) {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException("Invalid transaction type");
            }
            List<InputBaseTransactionData> inputBaseTransactions = transactionData.getInputBaseTransactions();
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
        try {
            return validateInputBaseTransactions(transactionData) && validateOutputBaseTransactions(transactionData);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
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

            List<OutputBaseTransactionData> outputBaseTransactions = transactionData.getOutputBaseTransactions();
            List<String> outputBaseTransactionNames = OutputBaseTransactionName.getOutputBaseTransactionsByType(type);
            if (outputBaseTransactionNames.size() != outputBaseTransactions.size()) {
                return false;
            }

            BigDecimal originalAmount = BigDecimal.ZERO;

            for (int i = 0; i < outputBaseTransactions.size(); i++) {
                OutputBaseTransactionData outputBaseTransactionData = outputBaseTransactions.get(i);
                if (!Class.forName(packagePath + outputBaseTransactionNames.get(i)).equals(outputBaseTransactionData.getClass())) {
                    return false;
                }
                if (!originalAmount.equals(BigDecimal.ZERO) && !originalAmount.equals((outputBaseTransactionData.getOriginalAmount()))) {
                    return false;
                }
                originalAmount = outputBaseTransactionData.getOriginalAmount();

            }

            return validateReducedAmount(outputBaseTransactions);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean validateReducedAmount(List<OutputBaseTransactionData> outputBaseTransactions) {
        BigDecimal reducedAmount = BigDecimal.ZERO;
        BigDecimal reducedTotalOutputTransactionAmount = BigDecimal.ZERO;

        for (OutputBaseTransactionData outputBaseTransactionData : outputBaseTransactions) {
            if (NetworkFeeData.class.isInstance(outputBaseTransactionData)) {
                reducedAmount = ((NetworkFeeData) outputBaseTransactionData).getReducedAmount();
            }
            if (!FullNodeFeeData.class.isInstance(outputBaseTransactionData)) {
                reducedTotalOutputTransactionAmount = reducedTotalOutputTransactionAmount.add(outputBaseTransactionData.getOriginalAmount());
            }
        }
        return reducedAmount.equals(reducedTotalOutputTransactionAmount);
    }

}
