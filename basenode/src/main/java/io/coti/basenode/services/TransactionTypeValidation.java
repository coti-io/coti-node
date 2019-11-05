package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.ITransactionTypeValidation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
public enum TransactionTypeValidation implements ITransactionTypeValidation {
    Payment(TransactionType.Payment) {
        @Override
        public boolean validateInputBaseTransactions(TransactionData transactionData) {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException(INVALID_TRANSACTION_TYPE);
            }
            List<InputBaseTransactionData> inputBaseTransactions = transactionData.getInputBaseTransactions();
            return inputBaseTransactions.stream().filter(inputBaseTransactionData -> inputBaseTransactionData instanceof PaymentInputBaseTransactionData).count() == 1
                    && inputBaseTransactions.get(0) instanceof PaymentInputBaseTransactionData;
        }
    },
    Transfer(TransactionType.Transfer) {
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
    ZeroSpend(TransactionType.ZeroSpend) {
        @Override
        public boolean validateInputBaseTransactions(TransactionData transactionData) {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException(INVALID_TRANSACTION_TYPE);
            }
            List<InputBaseTransactionData> inputBaseTransactions = transactionData.getInputBaseTransactions();
            return inputBaseTransactions.size() == 1 && inputBaseTransactions.get(0).getAmount().equals(BigDecimal.ZERO);
        }

    },
    Initial(TransactionType.Initial) {
        @Override
        public boolean validateBaseTransactions(TransactionData transactionData, Hash nativeCurrencyHash) {
            return validateBaseTransactions(transactionData, true, nativeCurrencyHash);
        }

        @Override
        public boolean validateInputBaseTransactions(TransactionData transactionData) {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException(INVALID_TRANSACTION_TYPE);
            }
            List<InputBaseTransactionData> inputBaseTransactions = transactionData.getInputBaseTransactions();
            return inputBaseTransactions.size() == 1;
        }

    },
    TokenGeneration(TransactionType.TokenGeneration) {
        @Override
        public boolean validateBaseTransactions(TransactionData transactionData, Hash nativeCurrencyHash) {
            return validateBaseTransactions(transactionData, true, nativeCurrencyHash);
        }
    };

    protected static final String INVALID_TRANSACTION_TYPE = "Invalid transaction type";
    protected static final String PACKAGE_PATH = "io.coti.basenode.data.";
    protected TransactionType type;

    TransactionTypeValidation(TransactionType type) {
        this.type = type;
    }

    @Override
    public boolean validateBaseTransactions(TransactionData transactionData, Hash nativeCurrencyHash) {
        return validateBaseTransactions(transactionData, false, nativeCurrencyHash);
    }

    protected boolean validateBaseTransactions(TransactionData transactionData, boolean skipValidationOfReducedAmount, Hash nativeCurrencyHash) {
        try {
            return validateInputBaseTransactions(transactionData) && validateOutputBaseTransactions(transactionData, skipValidationOfReducedAmount, nativeCurrencyHash);
        } catch (IllegalArgumentException e) {
            log.error("Errors of an illegal argument during validation of base transactions: {}", e.getMessage());
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
    public boolean validateOutputBaseTransactions(TransactionData transactionData, boolean skipValidationOfReducedAmount, Hash nativeCurrencyHash) {
        try {
            if (!type.equals(transactionData.getType())) {
                throw new IllegalArgumentException(INVALID_TRANSACTION_TYPE);
            }

            List<OutputBaseTransactionData> outputBaseTransactions = transactionData.getOutputBaseTransactions();
            List<String> outputBaseTransactionNames = OutputBaseTransactionName.getOutputBaseTransactionsByType(type);
            if (outputBaseTransactionNames.size() != outputBaseTransactions.size()) {
                return false;
            }

            BigDecimal originalAmount = BigDecimal.ZERO;
            Hash originalCurrencyHash = null;

            for (int i = 0; i < outputBaseTransactions.size(); i++) {
                OutputBaseTransactionData outputBaseTransactionData = outputBaseTransactions.get(i);
                if (!Class.forName(PACKAGE_PATH + outputBaseTransactionNames.get(i)).equals(outputBaseTransactionData.getClass())) {
                    return false;
                }
                if (!originalAmount.equals(BigDecimal.ZERO) && originalAmount.compareTo(outputBaseTransactionData.getOriginalAmount()) != 0) {
                    return false;
                }
                if (originalCurrencyHash != null &&
                        !originalCurrencyHash.equals(Optional.ofNullable(outputBaseTransactionData.getOriginalCurrencyHash()).orElse(nativeCurrencyHash))) {
                    return false;
                }

                originalAmount = outputBaseTransactionData.getOriginalAmount();
                if (originalCurrencyHash == null) {
                    originalCurrencyHash = Optional.ofNullable(outputBaseTransactionData.getOriginalCurrencyHash()).orElse(nativeCurrencyHash);
                }
            }

            return skipValidationOfReducedAmount || validateReducedAmount(outputBaseTransactions);
        } catch (ClassNotFoundException e) {
            log.error("Errors of class not found during validation of output base transactions: {}", e.getMessage());
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
