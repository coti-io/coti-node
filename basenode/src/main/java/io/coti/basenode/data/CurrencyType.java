package io.coti.basenode.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum CurrencyType {
    NATIVE_COIN("Native Coin"),
    REGULAR_CMD_TOKEN("Regular CMD Token"),
    RATED_CMD_TOKEN("Rated CMD Token"),
    PAYMENT_CMD_TOKEN("Payment CMD Token"),
    GLOBAL_PAYMENT_CMD_TOKEN("Global Payment CMD Token");

    private final String text;
    private static class CurrencyTypes {
        private static final Map<String, CurrencyType> textToCurrencyTypeMap = new HashMap<>();
    }

    CurrencyType(String text) {
        this.text = text;
        setToTextToTypeMap(text);
    }

    private void setToTextToTypeMap(String text) {
        CurrencyTypes.textToCurrencyTypeMap.put(text, this);
    }

    public String getText() {
        return text;
    }

    public static CurrencyType getByText(String text) {
        return Optional.ofNullable(CurrencyTypes.textToCurrencyTypeMap.get(text))
                .orElseThrow(() -> new IllegalArgumentException("No currency type exists for the giving text"));
    }

}
