package io.coti.trustscore.data.Enums;

public enum InitialTrustScoreType {
    KYC("KYC"),
    GENERAL_QUESTIONNAIRE("GeneralQuestionnaire"),
    MERCHANT_QUESTIONNAIRE("MerchantQuestionnaire");

    private String text;

    InitialTrustScoreType(String text) {
        this.text = text;
    }

    public static InitialTrustScoreType enumFromString(String text) {
        for (InitialTrustScoreType value : InitialTrustScoreType.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format("got event name %s, which not exists", text));
    }

    @Override
    public String toString() {
        return text;
    }
}
