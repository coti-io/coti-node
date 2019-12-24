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
        throw new IllegalArgumentException(String.format("Not existing event name %s", text));
    }

    @Override
    public String toString() {
        return text;
    }
}

// todo to delete