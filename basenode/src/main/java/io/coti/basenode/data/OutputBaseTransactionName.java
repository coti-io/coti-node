package io.coti.basenode.data;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum OutputBaseTransactionName {
    FULL_NODE_FEE_DATA(FullNodeFeeData.class, EnumSet.of(TransactionType.Payment, TransactionType.Transfer)),
    NETWORK_FEE_DATA(NetworkFeeData.class, EnumSet.of(TransactionType.Payment, TransactionType.Transfer)),
    ROLLING_RESERVE_DATA(RollingReserveData.class, EnumSet.of(TransactionType.Payment)),
    RECEIVER_BASE_TRANSACTION_DATA(ReceiverBaseTransactionData.class, EnumSet.of(TransactionType.Initial, TransactionType.Transfer, TransactionType.Payment));

    private final Class<? extends OutputBaseTransactionData> outputBaseTransactionClass;
    private final Set<TransactionType> transactionTypes;

    <T extends OutputBaseTransactionData> OutputBaseTransactionName(Class<T> outputBaseTransactionClass, Set<TransactionType> transactionTypes) {
        this.outputBaseTransactionClass = outputBaseTransactionClass;
        this.transactionTypes = transactionTypes;
    }

    public Class<? extends OutputBaseTransactionData> getOutputBaseTransactionClass() {
        return outputBaseTransactionClass;
    }

    private Set<TransactionType> getTransactionTypes() {
        return transactionTypes;
    }

    public static List<OutputBaseTransactionName> getOutputBaseTransactionsByType(TransactionType type) {
        List<OutputBaseTransactionName> outputBaseTransactions = new ArrayList<>();
        for (OutputBaseTransactionName outputBaseTransactionName : values()) {
            if (outputBaseTransactionName.getTransactionTypes().contains(type)) {
                outputBaseTransactions.add(outputBaseTransactionName);
            }
        }
        return outputBaseTransactions;
    }
}
