package io.coti.trustscore.data.scoreenums;


import io.coti.trustscore.data.scorebuckets.*;

public enum ScoreType {
    TRANSACTION(0, BucketTransactionScoreData.class),
    CHARGEBACK_SCORE(1, BucketChargeBackFrequencyBasedScoreData.class),
    EVENT_SCORE(2, BucketEventScoreData.class),
    DOCUMENT_SCORE(3, BucketDocumentScoreData.class),
    FREQUENCY_BASED_SCORE(4, BucketFrequencyBasedScoreData.class),
    DEBT_BALANCE_BASED_SCORE(5, BucketDebtBalanceBasedScoreData.class),
    DEPOSIT_BALANCE_BASED_SCORE(6, BucketDepositBalanceBasedScoreData.class);

    public int value;
    public Class bucket;


    ScoreType(int value, Class bucket) {
        this.value = value;
        this.bucket = bucket;
    }

    public int getValue() {
        return value;
    }
}
