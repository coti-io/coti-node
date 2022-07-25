package io.coti.basenode.data;

public enum RejectedTransactionDataReason {
    DATA_INTEGRITY,
    BALANCE,
    REJECTED_PARENT,
    TOKEN_UNIQUENESS,
    MINTING_BALANCE,
    EVENT_HARDFORK;
}