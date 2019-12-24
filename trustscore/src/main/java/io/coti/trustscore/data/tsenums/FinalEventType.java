package io.coti.trustscore.data.tsenums;

import io.coti.trustscore.data.tsevents.*;

import java.util.HashMap;
import java.util.Map;

public enum FinalEventType {
    KYC(KYCDocumentEventData.class),
    QUESTIONNAIRE1(Questionnaire1DocumentEventData.class),
    QUESTIONNAIRE2(Questionnaire2DocumentEventData.class),
    QUESTIONNAIRE3(Questionnaire3DocumentEventData.class),
    FILLQUESTIONNAIRE(FillQuestionnaireBehaviorEventData.class),
    FALSEQUESTIONNAIRE(FalseQuestionnaireBehaviorEventData.class),
    DOUBLESPENDING(DoubleSpendingBehaviorEventData.class),
    INVALIDTX(InvalidTxBehaviorEventData.class),
    CHARGEBACK(ChargeBackFrequencyBasedEventData.class),
    CLAIM(ClaimFrequencyBasedEventData.class),
    DEBT(DebtBalanceBasedEventData.class),
    CLOSEDEBT(CloseDebtBalanceBasedEventData.class),
    DEPOSIT(DepositBalanceBasedEventData.class),
    CLOSEDEPOSIT(CloseDepositBalanceBasedEventData.class),
    TRANSACTION(TransactionEventData.class);

    private String text;
    private static final Map<String, FinalEventType> labelsMap = new HashMap<>();

    static {
        for (FinalEventType e: values()) {
            labelsMap.put(e.text, e);
        }
    }

    FinalEventType(Class c) {
        this.text = c.getSimpleName();
    }

    public static FinalEventType enumFromString(String text) {
        return labelsMap.get(text);
    }

    @Override
    public String toString() {
        return text;
    }
}
