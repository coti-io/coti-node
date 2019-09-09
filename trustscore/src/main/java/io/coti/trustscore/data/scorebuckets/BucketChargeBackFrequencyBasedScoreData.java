package io.coti.trustscore.data.scorebuckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.scoreevents.ChargeBackFrequencyBasedScoreData;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketChargeBackFrequencyBasedScoreData extends BucketData<ChargeBackFrequencyBasedScoreData> implements IEntity {

    private static final long serialVersionUID = -5789443358192063490L;
    private Map<LocalDate, Double> currentMonthAmountOfChargeBacks;
    private Map<LocalDate, Integer> currentMonthNumberOfChargeBacks;
    private Map<LocalDate, Double> currentMonthAmountOfPaymentTransactions;
    private Map<LocalDate, Integer> currentMonthNumberOfPaymentTransactions;

    private double currentMonthContributionOfChargeBacksAmount = 0.0;
    private double oldDatesContributionOfChargeBacksAmount = 0.0;
    private double currentMonthContributionOfChargeBacksNumber = 0.0;
    private double oldDatesContributionOfChargeBacksNumber = 0.0;

    public BucketChargeBackFrequencyBasedScoreData() {
        super();
        currentMonthAmountOfChargeBacks = new ConcurrentHashMap<>();
        currentMonthNumberOfChargeBacks = new ConcurrentHashMap<>();
        currentMonthAmountOfPaymentTransactions = new ConcurrentHashMap<>();
        currentMonthNumberOfPaymentTransactions = new ConcurrentHashMap<>();
    }
}
