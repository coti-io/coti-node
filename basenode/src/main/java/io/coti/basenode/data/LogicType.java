package io.coti.basenode.data;

public enum LogicType {
    FORCE_DSPC_FOR_TCC(1, 700000);

    private final long ruleIndex;
    private final long transactionIndex;

    LogicType(long ruleIndex, long transactionIndex) {
        this.ruleIndex = ruleIndex;
        this.transactionIndex = transactionIndex;
    }

    public long getRuleIndex() {
        return ruleIndex;
    }

    public long getTransactionIndex() {
        return transactionIndex;
    }
}
