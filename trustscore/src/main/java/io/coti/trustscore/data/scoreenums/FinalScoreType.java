package io.coti.trustscore.data.scoreenums;

public enum FinalScoreType {
    KYC("KYCDocumentScoreData"),
    QUESTIONNAIRE1("Questionnaire1DocumentScoreData"),
    QUESTIONNAIRE2("Questionnaire2DocumentScoreData"),
    QUESTIONNAIRE3("Questionnaire3DocumentScoreData"),
    FILLQUESTIONNAIRE("FillQuestionnaireEventScoreData"),
    FALSEQUESTIONNAIRE("FalseQuestionnaireEventScoreData"),
    DOUBLESPENDING("DoubleSpendingEventScoreData"),
    INVALIDTX("InvalidTxEventScoreData"),
    CHARGEBACK("ChargeBackFrequencyBasedScoreData"),
    CLAIM("ClaimFrequencyBasedScoreData"),
    DEBT("DebtBalanceBasedScoreData"),
    CLOSEDEBT("CloseDebtBalanceBasedScoreData"),
    DEPOSIT("DepositBalanceBasedScoreData"),
    CLOSEDEPOSIT("CloseDepositBalanceBasedScoreData"),
    TRANSACTION("TransactionScoreData");

    private String text;

    FinalScoreType(String text) {
        this.text = text;
    }

    public static FinalScoreType enumFromString(String text) {
        for (FinalScoreType value : FinalScoreType.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format("Score type {} doesn't exist", text));
    }

    @Override
    public String toString() {
        return text;
    }
}
