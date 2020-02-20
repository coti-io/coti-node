package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.ITransactionTypeValidation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public enum TransactionTypeValidation implements ITransactionTypeValidation {
    PAYMENT(TransactionType.Payment) {
        @Override
        public boolean validateInputBaseTransactions(TransactionData transactionData) {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException(INVALID_TRANSACTION_TYPE);
            }
            List<InputBaseTransactionData> inputBaseTransactions = transactionData.getInputBaseTransactions();
            return inputBaseTransactions.stream().filter(PaymentInputBaseTransactionData.class::isInstance).count() == 1
                    && inputBaseTransactions.get(0) instanceof PaymentInputBaseTransactionData;
        }
    },
    TRANSFER(TransactionType.Transfer) {
        @Override
        public boolean validateReducedAmount(List<OutputBaseTransactionData> outputBaseTransactions) {
            BigDecimal reducedAmount = BigDecimal.ZERO;
            BigDecimal fullNodeFeeAmount = BigDecimal.ZERO;

            for (OutputBaseTransactionData outputBaseTransactionData : outputBaseTransactions) {
                if (outputBaseTransactionData instanceof FullNodeFeeData) {
                    fullNodeFeeAmount = outputBaseTransactionData.getAmount();
                } else if (outputBaseTransactionData instanceof NetworkFeeData) {
                    reducedAmount = ((NetworkFeeData) outputBaseTransactionData).getReducedAmount();
                } else if (outputBaseTransactionData instanceof ReceiverBaseTransactionData) {
                    return (reducedAmount == null && outputBaseTransactionData.getOriginalAmount().compareTo(outputBaseTransactionData.getAmount()) == 0) ||
                            (reducedAmount != null && outputBaseTransactionData.getOriginalAmount().compareTo(reducedAmount.add(fullNodeFeeAmount)) == 0);
                }
            }
            return false;
        }
    },
    ZERO_SPEND(TransactionType.ZeroSpend) {
        @Override
        public boolean validateInputBaseTransactions(TransactionData transactionData) {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException(INVALID_TRANSACTION_TYPE);
            }
            List<InputBaseTransactionData> inputBaseTransactions = transactionData.getInputBaseTransactions();
            return inputBaseTransactions.size() == 1 && inputBaseTransactions.get(0).getAmount().equals(BigDecimal.ZERO);
        }

    },
    INITIAL(TransactionType.Initial) {
        @Override
        public boolean validateBaseTransactions(TransactionData transactionData) {
            try {
                return validateInputBaseTransactions(transactionData) && validateOutputBaseTransactions(transactionData, true);
            } catch (IllegalArgumentException e) {
                log.error("Errors during validation of base transactions: ", e);
                return false;
            }
        }

        @Override
        public boolean validateInputBaseTransactions(TransactionData transactionData) {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException(INVALID_TRANSACTION_TYPE);
            }
            List<InputBaseTransactionData> inputBaseTransactions = transactionData.getInputBaseTransactions();
            return inputBaseTransactions.size() == 1;
        }

    };

    protected static final String INVALID_TRANSACTION_TYPE = "Invalid transaction type";
    protected TransactionType type;

    TransactionTypeValidation(TransactionType type) {
        this.type = type;
    }

    public static TransactionTypeValidation getByType(TransactionType transactionType) {
        for (TransactionTypeValidation transactionTypeValidation : values()) {
            if (transactionTypeValidation.type.equals(transactionType)) {
                return transactionTypeValidation;
            }
        }
        throw new IllegalArgumentException(INVALID_TRANSACTION_TYPE);
    }

    @Override
    public boolean validateBaseTransactions(TransactionData transactionData) {
        try {
            return validateInputBaseTransactions(transactionData) && validateOutputBaseTransactions(transactionData, false);
        } catch (IllegalArgumentException e) {
            log.error("Errors of an illegal argument during validation of base transactions: ", e);
            return false;
        }
    }

    @Override
    public boolean validateInputBaseTransactions(TransactionData transactionData) {
        if (!type.equals(transactionData.getType())) {
            throw new IllegalArgumentException(INVALID_TRANSACTION_TYPE);
        }
        return true;
    }

    @Override
    public boolean validateOutputBaseTransactions(TransactionData transactionData, boolean skipValidationOfReducedAmount) {
        try {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException(INVALID_TRANSACTION_TYPE);
            }

            List<OutputBaseTransactionData> outputBaseTransactions = transactionData.getOutputBaseTransactions();
            List<OutputBaseTransactionName> outputBaseTransactionNames = OutputBaseTransactionName.getOutputBaseTransactionsByType(type);
            if (outputBaseTransactionNames.size() != outputBaseTransactions.size()) {
                return false;
            }

            BigDecimal originalAmount = BigDecimal.ZERO;

            for (int i = 0; i < outputBaseTransactions.size(); i++) {
                OutputBaseTransactionData outputBaseTransactionData = outputBaseTransactions.get(i);
                if (!outputBaseTransactionNames.get(i).getOutputBaseTransactionClass().isInstance(outputBaseTransactionData)) {
                    return false;
                }
                if (!originalAmount.equals(BigDecimal.ZERO) && originalAmount.compareTo(outputBaseTransactionData.getOriginalAmount()) != 0) {
                    return false;
                }
                originalAmount = outputBaseTransactionData.getOriginalAmount();

            }

            return skipValidationOfReducedAmount || validateReducedAmount(outputBaseTransactions);
        } catch (Exception e) {
            log.error("Errors of class not found during validation of output base transactions: ", e);
            return false;
        }
    }

    @Override
    public boolean validateReducedAmount(List<OutputBaseTransactionData> outputBaseTransactions) {
        BigDecimal reducedAmount = BigDecimal.ZERO;
        BigDecimal reducedTotalOutputTransactionAmount = BigDecimal.ZERO;

        for (OutputBaseTransactionData outputBaseTransactionData : outputBaseTransactions) {
            if (outputBaseTransactionData instanceof NetworkFeeData) {
                reducedAmount = ((NetworkFeeData) outputBaseTransactionData).getReducedAmount();
            }
            if (!(outputBaseTransactionData instanceof FullNodeFeeData)) {
                reducedTotalOutputTransactionAmount = reducedTotalOutputTransactionAmount.add(outputBaseTransactionData.getAmount());
            }
        }
        return reducedAmount.compareTo(reducedTotalOutputTransactionAmount) == 0;
    }

}
