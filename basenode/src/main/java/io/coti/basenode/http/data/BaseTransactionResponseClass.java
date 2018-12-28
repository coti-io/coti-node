package io.coti.basenode.http.data;

public enum BaseTransactionResponseClass {
    IBT(InputBaseTransactionResponseData.class),
    PIBT(PaymentInputBaseTransactionResponseData.class),
    FFBT(FullNodeFeeResponseData.class),
    NFBT(NetworkFeeResponseData.class),
    RRBT(RollingReserveResponseData.class),
    RBT(ReceiverBaseTransactionResponseData.class);

    private Class<? extends BaseTransactionResponseData> baseTransactionResponseClass;

    <T extends BaseTransactionResponseData> BaseTransactionResponseClass(Class<T> baseTransactionResponseClass) {
        this.baseTransactionResponseClass = baseTransactionResponseClass;
    }

    public  Class<? extends BaseTransactionResponseData> getBaseTransactionResponseClass() {
        return baseTransactionResponseClass;
    }
}
