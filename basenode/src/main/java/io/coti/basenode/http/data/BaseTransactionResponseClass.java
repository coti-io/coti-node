package io.coti.basenode.http.data;

public enum BaseTransactionResponseClass {
    IBT(InputBaseTransactionResponseData.class),
    PIBT(PaymentInputBaseTransactionResponseData.class),
    FFBT(FullNodeFeeResponseData.class),
    NFBT(NetworkFeeResponseData.class),
    RRBT(RollingReserveResponseData.class),
    RBT(ReceiverBaseTransactionResponseData.class);

    private Class<? extends BaseTransactionResponseData> responseClass;

    <T extends BaseTransactionResponseData> BaseTransactionResponseClass(Class<T> responseClass) {
        this.responseClass = responseClass;
    }

    public Class<? extends BaseTransactionResponseData> getResponseClass() {
        return responseClass;
    }
}
