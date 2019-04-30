package io.coti.financialserver.data;

public enum TokenSale {

    PRIVATE_SALE(ReservedAddress.PRIVATE_SALE, "Private Sale"),
    PRE_SALE(ReservedAddress.PRE_SALE, "Pre Sale"),
    EQUITY_INVESTORS(ReservedAddress.EQUITY_INVESTORS, "Equity Investors"),
    ;

    private ReservedAddress reservedAddress;
    private String text;

    TokenSale(ReservedAddress reservedAddress, String text) {
        this.reservedAddress = reservedAddress;
        this.text = text;
    }

    public static long getIndexByText(String text) {
        long indexByText = -1;
        for ( TokenSale tokenSale : values() ) {
            if( tokenSale.text.contentEquals(text) )
                indexByText = tokenSale.reservedAddress.getIndex();
        }
        return indexByText;
    }


}
