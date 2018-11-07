package io.coti.trustscore.data.Buckets;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.ChargeBackEventsData;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketChargeBackEventsData extends BucketEventData<ChargeBackEventsData> implements IEntity {
    private UserType userType;
    private Hash bucketHash;

    private Map<Hash, Double> currentDateChargeBacks;
    private Map<Hash, Double> currentDatePaymentTransactions;

    private double oldDateAmountOfChargeBacksContribution;
    private double oldDateAmountOfCreditTransactionsContribution;

    private double oldDateNumberOfChargeBacksContribution;
    private double oldDateNumberOfCreditTransactionsContribution;

    private double totalContributionOfChargeBacksAndCreditsAmountContribution;
    private double totalContributionOfChargeBacksAndCreditsNumberContribution;

    private Map<Hash, Date> oldDateChargeBacks;
    private Map<Hash, Date> oldDatePaymentTransactions;

    public BucketChargeBackEventsData() {
        currentDateChargeBacks = new ConcurrentHashMap<>();
        currentDatePaymentTransactions = new ConcurrentHashMap<>();
        oldDateChargeBacks = new ConcurrentHashMap<>();
        oldDatePaymentTransactions = new ConcurrentHashMap<>();
    }

    @Override
    public Hash getHash() {
        return this.bucketHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.bucketHash = hash;
    }

}
