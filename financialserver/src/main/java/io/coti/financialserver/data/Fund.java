package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;

import static io.coti.basenode.services.BaseNodeServiceManager.nodeIdentityService;

public enum Fund {

    TOKEN_SALE(ReservedAddress.TOKEN_SALE, "TokenSale"),
    INCENTIVES(ReservedAddress.INCENTIVES, "Incentives"),
    TEAM(ReservedAddress.TEAM, "Team"),
    ADVISORS(ReservedAddress.ADVISORS, "Advisers");


    private final ReservedAddress reservedAddress;
    private final String text;
    private final Hash fundHash;

    Fund(ReservedAddress reservedAddress, String text) {
        this.reservedAddress = reservedAddress;
        this.text = text;
        this.fundHash = nodeIdentityService.generateAddress(Math.toIntExact(this.reservedAddress.getIndex()));
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

    public static Fund getTokenSaleRelatedFundNameByText(String text) {
        for (Fund fund : values()) {
            if (fund.text.contentEquals(text) && fund.isTokenSaleRelatedFund()) {
                return fund;
            }
        }
        throw new IllegalArgumentException("Invalid fund name");
    }

    public boolean isTokenSaleRelatedFund() {
        return reservedAddress.isTokenSaleRelatedFund();
    }

    public static Fund getFundByText(String text) {
        for (Fund fund : values()) {
            if (fund.text.contentEquals(text) && fund.isSecondaryFundDistribution()) {
                return fund;
            }
        }
        throw new IllegalArgumentException("Invalid fund name");
    }

    public boolean isSecondaryFundDistribution() {
        return reservedAddress.isSecondaryFundDistribution();
    }

}
