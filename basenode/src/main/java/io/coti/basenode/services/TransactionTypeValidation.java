package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.ITransactionTypeValidation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public enum TransactionTypeValidation implements ITransactionTypeValidation {
    Payment(TransactionType.Payment) {
        @Override
        public boolean validateInputBaseTransactions(TransactionData transactionData) {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException("Invalid transaction type");
            }
            List<InputBaseTransactionData> inputBaseTransactions = transactionData.getInputBaseTransactions();
            return inputBaseTransactions.stream().filter(inputBaseTransactionData -> PaymentInputBaseTransactionData.class.isInstance(inputBaseTransactionData)).count() == 1
                    && PaymentInputBaseTransactionData.class.isInstance(inputBaseTransactions.get(0));
        }
    },
    Transfer(TransactionType.Transfer) {
        @Override
        public boolean validateReducedAmount(List<OutputBaseTransactionData> outputBaseTransactions) {
            BigDecimal reducedAmount = BigDecimal.ZERO;
            BigDecimal fullNodeFeeAmount = BigDecimal.ZERO;

            for (OutputBaseTransactionData outputBaseTransactionData : outputBaseTransactions) {
                if (FullNodeFeeData.class.isInstance(outputBaseTransactionData)) {
                    fullNodeFeeAmount = outputBaseTransactionData.getAmount();
                } else if (NetworkFeeData.class.isInstance(outputBaseTransactionData)) {
                    reducedAmount = ((NetworkFeeData) outputBaseTransactionData).getReducedAmount();
                } else if (ReceiverBaseTransactionData.class.isInstance(outputBaseTransactionData)) {
                    return outputBaseTransactionData.getAmount().compareTo(reducedAmount.add(fullNodeFeeAmount)) == 0;
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
                if (!originalAmount.equals(BigDecimal.ZERO) && originalAmount.compareTo(outputBaseTransactionData.getOriginalAmount()) != 0) {
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
                reducedTotalOutputTransactionAmount = reducedTotalOutputTransactionAmount.add(outputBaseTransactionData.getAmount());
            }
        }
        return reducedAmount.stripTrailingZeros().equals(reducedTotalOutputTransactionAmount.stripTrailingZeros());
    }

}
