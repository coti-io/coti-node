package io.coti.financialserver.data;

public enum TokenSale {

    PRIVATE_SALE(ReservedAddress.PRIVATE_SALE, "Private Sale"),
    PRE_SALE(ReservedAddress.PRE_SALE, "Pre Sale"),
    EQUITY_INVESTORS(ReservedAddress.EQUITY_INVESTORS, "Equity Investors"),;

    private ReservedAddress reservedAddress;
    private String text;

    TokenSale(ReservedAddress reservedAddress, String text) {
        this.reservedAddress = reservedAddress;
        this.text = text;
    }

    public ReservedAddress getReservedAddress() {
        return reservedAddress;
    }

    public String getText() {
        return text;
    }

    public static TokenSale getNameByText(String text) throws IllegalArgumentException {
        for (TokenSale tokenSale : values()) {
            if (tokenSale.text.contentEquals(text))
                return tokenSale;
        }
        throw new IllegalArgumentException("Invalid fund name");
    }


}
