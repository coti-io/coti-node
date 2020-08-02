package io.coti.basenode.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum CurrencyRateSourceType {
    ADDRESS("Address"),
    FINANCIAL_SERVER("Financial Server"),
    RATES_SERVER("Rates Server");

    private String text;

    private static class CurrencyRateSourceTypes {
        private static final Map<String, CurrencyRateSourceType> textToCurrencyRateSourceTypeMap = new HashMap<>();
    }

    CurrencyRateSourceType(String text) {
        this.text = text;
        CurrencyRateSourceTypes.textToCurrencyRateSourceTypeMap.put(text, this);
    }

    public String getText() {
        return text;
    }

    public static CurrencyRateSourceType getByText(String text) {
        return Optional.ofNullable(CurrencyRateSourceTypes.textToCurrencyRateSourceTypeMap.get(text))
                .orElseThrow(() -> new IllegalArgumentException("No currency rate source type exists for the giving text"));
    }

}
