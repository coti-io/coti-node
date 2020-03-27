package io.coti.basenode.data;

public enum CurrencyRateSourceType {
    ADDRESS("Address"),
    FINANCIAL_SERVER("Financial Server"),
    RATES_SERVER("Rates Server");

    private String text;

    CurrencyRateSourceType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static CurrencyRateSourceType getByText(String text) {
        for (CurrencyRateSourceType currencyType : values()) {
            if (currencyType.text.equals(text)) {
                return currencyType;
            }
        }
        throw new IllegalArgumentException("No currency rate source type exists for the giving text");
    }

}
