package io.coti.basenode.services;

import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.crypto.TokenMintingCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.ITransactionTypeValidation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
    EVENT_HARD_FORK(TransactionType.EventHardFork) {
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
            if (inputBaseTransactions.size() != 1) {
                return false;
            }
            InputBaseTransactionData inputBaseTransactionData = inputBaseTransactions.get(0);
            if (!inputBaseTransactionData.getAmount().equals(BigDecimal.ZERO)
                    || !(inputBaseTransactionData instanceof EventInputBaseTransactionData)) {
                return false;
            }
            EventInputBaseTransactionData eventInputBaseTransactionData = (EventInputBaseTransactionData) inputBaseTransactionData;
            return eventInputBaseTransactionData.getEvent().isHardFork();
        }

    },
    TOKEN_GENERATION(TransactionType.TokenGeneration) {
        @Override
        public boolean validateBaseTransactions(TransactionData transactionData, Hash nativeCurrencyHash) {
            return validateBaseTransactions(transactionData, true, nativeCurrencyHash) && validateTokenGenerationData(transactionData);
        }

        private boolean validateTokenGenerationData(TransactionData transactionData) {
            List<OutputBaseTransactionData> outputBaseTransactions = transactionData.getOutputBaseTransactions();
            for (OutputBaseTransactionData outputBaseTransactionData : outputBaseTransactions) {
                if (outputBaseTransactionData instanceof TokenGenerationFeeBaseTransactionData) {
                    TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData = (TokenGenerationFeeBaseTransactionData) outputBaseTransactionData;
                    TokenGenerationServiceData tokenGenerationServiceData = tokenGenerationFeeBaseTransactionData.getServiceData();
                    OriginatorCurrencyData originatorCurrencyData = tokenGenerationServiceData.getOriginatorCurrencyData();
                    CurrencyTypeData currencyTypeData = tokenGenerationServiceData.getCurrencyTypeData();
                    CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(originatorCurrencyData.getSymbol(), currencyTypeData);
                    return originatorCurrencyCrypto.verifySignature(originatorCurrencyData) && currencyTypeRegistrationCrypto.verifySignature(currencyTypeRegistrationData)
                            && tokenGenerationServiceData.getFeeAmount().equals(tokenGenerationFeeBaseTransactionData.getAmount());
                }
            }
            return true;
        }
    },
    TOKEN_MINTING(TransactionType.TokenMinting) {
        @Override
        public boolean validateBaseTransactions(TransactionData transactionData, Hash nativeCurrencyHash) {
            return validateBaseTransactions(transactionData, true, nativeCurrencyHash) && validateTokenMintingData(transactionData);
        }

        private boolean validateTokenMintingData(TransactionData transactionData) {
            List<OutputBaseTransactionData> outputBaseTransactions = transactionData.getOutputBaseTransactions();
            for (OutputBaseTransactionData outputBaseTransactionData : outputBaseTransactions) {
                if (outputBaseTransactionData instanceof TokenMintingFeeBaseTransactionData) {
                    TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = (TokenMintingFeeBaseTransactionData) outputBaseTransactionData;
                    TokenMintingServiceData tokenMintingServiceData = tokenMintingFeeBaseTransactionData.getServiceData();
                    return tokenMintingCrypto.verifySignature(tokenMintingServiceData) && (tokenMintingServiceData.getFeeAmount() == null || tokenMintingServiceData.getFeeAmount().equals(tokenMintingFeeBaseTransactionData.getAmount()));
                }
            }
            return true;
        }
    };

    protected static final String INVALID_TRANSACTION_TYPE = "Invalid transaction type";
    protected final TransactionType type;
    protected OriginatorCurrencyCrypto originatorCurrencyCrypto;
    protected CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    protected TokenMintingCrypto tokenMintingCrypto;
    protected ICurrencyService currencyService;

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
    public boolean validateBaseTransactions(TransactionData transactionData, Hash nativeCurrencyHash) {
        return validateBaseTransactions(transactionData, false, nativeCurrencyHash);
    }

    protected boolean validateBaseTransactions(TransactionData transactionData, boolean skipValidationOfReducedAmount, Hash nativeCurrencyHash) {
        try {
            return validateInputBaseTransactions(transactionData) && validateOutputBaseTransactions(transactionData, skipValidationOfReducedAmount, nativeCurrencyHash);
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
    public boolean validateOutputBaseTransactions(TransactionData transactionData, boolean skipValidationOfReducedAmount, Hash nativeCurrencyHash) {
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
            Hash originalCurrencyHash = null;

            for (int i = 0; i < outputBaseTransactions.size(); i++) {
                OutputBaseTransactionData outputBaseTransactionData = outputBaseTransactions.get(i);
                if (!outputBaseTransactionNames.get(i).getOutputBaseTransactionClass().isInstance(outputBaseTransactionData)) {
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
                originalCurrencyHash = Optional.ofNullable(outputBaseTransactionData.getOriginalCurrencyHash()).orElse(nativeCurrencyHash);
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
