package io.coti.basenode.data;

@SuppressWarnings("java:S115")
public enum TransactionType {
    Initial,
    Payment,
    Transfer,
    ZeroSpend,
    Chargeback,
    TokenGeneration,
    TokenMinting,
    EventHardFork
}
