package io.coti.trustscore.data.tsenums;


import io.coti.trustscore.data.tsbuckets.*;

public enum EventType {
    TRANSACTION(0, BucketTransactionEventData.class),
    CHARGEBACK_SCORE(1, BucketChargeBackFrequencyBasedEventData.class),
    EVENT_SCORE(2, BucketBehaviorEventData.class),
    DOCUMENT_SCORE(3, BucketDocumentEventData.class),
    FREQUENCY_BASED_SCORE(4, BucketFrequencyBasedEventData.class),
    DEBT_BALANCE_BASED_SCORE(5, BucketDebtBalanceBasedEventData.class),
    DEPOSIT_BALANCE_BASED_SCORE(6, BucketDepositBalanceBasedEventData.class);

    private int value;
    private Class bucket;

    EventType(int value, Class bucket) {
        this.value = value;
        this.bucket = bucket;
    }

    public int getValue() {
        return value;
    }
    public Class getBucket() {
        return bucket;
    }

}
