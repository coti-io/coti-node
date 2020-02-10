package io.coti.trustscore.data.Enums;

public enum HighFrequencyEventScoreType {

    CLAIM("Claim"),
    CHARGE_BACK("ChargeBack"),
    CHARGE_BACK_AMOUNT("ChargeBackAmount"),
    CHARGE_BACK_NUMBER("ChargeBackNumber");

    private String text;

    HighFrequencyEventScoreType(String text) {
        this.text = text;
    }

    public static HighFrequencyEventScoreType enumFromString(String text) {
        for (HighFrequencyEventScoreType value : HighFrequencyEventScoreType.values()) {
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
