package io.coti.trustscore.data.Buckets;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.ChargeBackEventsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class BucketChargeBackEventsData extends BucketEventData<ChargeBackEventsData> implements IEntity {

    private static final long serialVersionUID = 7686600318847325729L;
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
        super.setEventType(EventType.HIGH_FREQUENCY_EVENTS);
        currentDateChargeBacks = new ConcurrentHashMap<>();
        currentDatePaymentTransactions = new ConcurrentHashMap<>();
        oldDateChargeBacks = new ConcurrentHashMap<>();
        oldDatePaymentTransactions = new ConcurrentHashMap<>();
    }

}
