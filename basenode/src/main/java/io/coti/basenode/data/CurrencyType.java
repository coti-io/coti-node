package io.coti.basenode.data;

public enum CurrencyType {
    NATIVE_COIN("Native Coin"),
    REGULAR_CMD_TOKEN("Regular CMD Token"),
    RATED_CMD_TOKEN("Rated CMD Token"),
    PAYMENT_CMD_TOKEN("Payment CMD Token"),
    GLOBAL_PAYMENT_CMD_TOKEN("Global Payment CMD Token");

    private String text;

    CurrencyType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static CurrencyType getByText(String text) {
        for (CurrencyType currencyType : values()) {
            if (currencyType.text.equals(text)) {
                return currencyType;
            }
        }
        throw new IllegalArgumentException("No currency type exists for the giving text");
    }

}
