package io.coti.basenode.data;

public enum BaseTransactionName {
    IBT(InputBaseTransactionData.class),
    PIBT(PaymentInputBaseTransactionData.class),
    EIBT(EventInputBaseTransactionData.class),
    FFBT(FullNodeFeeData.class),
    NFBT(NetworkFeeData.class),
    TGBT(TokenGenerationFeeBaseTransactionData.class),
    TMBT(TokenMintingFeeBaseTransactionData.class),
    RRBT(RollingReserveData.class),
    RBT(ReceiverBaseTransactionData.class);

    private final Class<? extends BaseTransactionData> baseTransactionClass;

    <T extends BaseTransactionData> BaseTransactionName(Class<T> baseTransactionClass) {
        this.baseTransactionClass = baseTransactionClass;
    }

    public Class<? extends BaseTransactionData> getBaseTransactionClass() {
        return baseTransactionClass;
    }

    public static BaseTransactionName getName(Class<?> baseTransactionClass) {
        for (BaseTransactionName name : values()) {
            if (name.getBaseTransactionClass() == baseTransactionClass) {
                return name;
            }
        }
        return null;
    }
}
