package io.coti.trustscore.data.scoreenums;

public enum TrustScoreRangeType {
    LOW("low"),
    STANDARD("standard"),
    HIGH("high");

    private String text;

    TrustScoreRangeType(String text) {
        this.text = text;
    }

    public static TrustScoreRangeType enumFromString(String text) {
        for (TrustScoreRangeType value : TrustScoreRangeType.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format("got trust score range type name {}, which not exists", text));
    }

    @Override
    public String toString() {
        return text;
    }
}
