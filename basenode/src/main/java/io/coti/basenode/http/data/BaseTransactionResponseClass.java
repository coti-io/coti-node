package io.coti.basenode.http.data;

public enum BaseTransactionResponseClass {
    IBT(InputBaseTransactionResponseData.class),
    PIBT(PaymentInputBaseTransactionResponseData.class),
    FFBT(FullNodeFeeResponseData.class),
    NFBT(NetworkFeeResponseData.class),
    TGBT(TokenGenerationServiceFeeResponseData.class),
    TMBT(TokenMintingServiceFeeResponseData.class),
    RRBT(RollingReserveResponseData.class),
    RBT(ReceiverBaseTransactionResponseData.class),
    EIBT(EventInputBaseTransactionResponseData.class);

    private Class<? extends BaseTransactionResponseData> responseClass;

    <T extends BaseTransactionResponseData> BaseTransactionResponseClass(Class<T> responseClass) {
        this.responseClass = responseClass;
    }

    public Class<? extends BaseTransactionResponseData> getResponseClass() {
        return responseClass;
    }
}
