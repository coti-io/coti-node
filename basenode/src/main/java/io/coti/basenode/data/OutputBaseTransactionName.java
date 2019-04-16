package io.coti.basenode.data;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum OutputBaseTransactionName {
    FullNodeFeeData(EnumSet.of(TransactionType.Payment, TransactionType.Transfer)),
    NetworkFeeData(EnumSet.of(TransactionType.Payment, TransactionType.Transfer)),
    RollingReserveData(EnumSet.of(TransactionType.Payment)),
    ReceiverBaseTransactionData(EnumSet.of(TransactionType.Initial, TransactionType.Transfer, TransactionType.Payment));

    private Set<TransactionType> transactionTypes;

    OutputBaseTransactionName(Set<TransactionType> transactionTypes) {
        this.transactionTypes = transactionTypes;
    }

    private Set<TransactionType> getTransactionTypes() {
        return transactionTypes;
    }

    public static List<String> getOutputBaseTransactionsByType(TransactionType type) {
        List<String> outputBaseTransactions = new ArrayList<>();
        for (OutputBaseTransactionName outputBaseTransactionName : values()) {
            if (outputBaseTransactionName.getTransactionTypes().contains(type)) {
                outputBaseTransactions.add(outputBaseTransactionName.name());
            }
        }
        return outputBaseTransactions;
    }
}
