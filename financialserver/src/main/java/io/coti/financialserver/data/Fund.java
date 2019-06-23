package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;

public enum Fund {

    TOKEN_SALE(ReservedAddress.TOKEN_SALE, "TokenSale"),
    INCENTIVES(ReservedAddress.INCENTIVES, "Incentives"),
    TEAM(ReservedAddress.TEAM, "Team"),
    ADVISORS(ReservedAddress.ADVISORS, "Advisers");


    private ReservedAddress reservedAddress;
    private String text;
    private Hash fundHash;

    Fund(ReservedAddress reservedAddress, String text) {
        this.reservedAddress = reservedAddress;
        this.text = text;
        this.fundHash = null;
    }

    public ReservedAddress getReservedAddress() {
        return reservedAddress;
    }

    public String getText() {
        return text;
    }

    public Hash getFundHash() {
        return fundHash;
    }

    public void setFundHash(Hash fundHash) {
        this.fundHash = fundHash;
    }

    public static Fund getTokenSaleRelatedFundNameByText(String text) {
        for (Fund fund : values()) {
            if (fund.text.contentEquals(text) && fund.isTokenSaleRelatedFund())
                return fund;
        }
        throw new IllegalArgumentException("Invalid fund name");
    }

    public boolean isTokenSaleRelatedFund() {
        return reservedAddress.isTokenSaleRelatedFund();
    }

    public static Fund getFundByText(String text) {
        for (Fund fund : values()) {
            if (fund.text.contentEquals(text) && fund.isSecondaryFundDistribution())
                return fund;
        }
        throw new IllegalArgumentException("Invalid fund name");
    }

    public boolean isSecondaryFundDistribution() {
        return reservedAddress.isSecondaryFundDistribution();
    }

}
