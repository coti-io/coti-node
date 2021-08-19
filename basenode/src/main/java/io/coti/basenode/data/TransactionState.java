package io.coti.basenode.data;

public enum TransactionState {
    RECEIVED,
    PRE_BALANCE_CHANGED,
    PAYLOAD_CHECKED,
    SAVED_IN_DB,
    FINISHED
}
