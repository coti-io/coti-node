package io.coti.trustscore.data.enums;

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
        throw new IllegalArgumentException(String.format("got an event name %s, which does not exist", text));
    }

    @Override
    public String toString() {
        return text;
    }

}
