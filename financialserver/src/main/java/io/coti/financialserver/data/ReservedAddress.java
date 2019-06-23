package io.coti.financialserver.data;

import java.util.EnumSet;

public enum ReservedAddress {
    GENESIS_ONE(0),
    GENESIS_TWO(1),
    TOKEN_SALE(2),
    INCENTIVES(3),
    TEAM(4),
    ADVISORS(5),
    PRIVATE_SALE(6),
    PRE_SALE(7),
    EQUITY_INVESTORS(8),
    ROLLING_RESERVE_POOL(9),
    NETWORK_FEE_POOL(10);

    private final long index;

    ReservedAddress(long index) {
        this.index = index;
    }

    public long getIndex() {
        return index;
    }

    public boolean isGenesisAddress() {
        return index == GENESIS_ONE.getIndex() || index == GENESIS_TWO.getIndex();
    }

    public boolean isInitialFundDistribution() {
        return index > GENESIS_TWO.getIndex() && index < PRIVATE_SALE.getIndex();
    }

    public boolean isTokenSaleRelatedFund() {
        return index > ADVISORS.getIndex() && index < ROLLING_RESERVE_POOL.getIndex();
    }

    public boolean isSecondaryFundDistribution() {
        return index > GENESIS_TWO.getIndex() && index < PRIVATE_SALE.getIndex();
    }

    public static EnumSet<ReservedAddress> getInitialFundDistributionAddresses() {
        EnumSet<ReservedAddress> initialFundDistributionAddresses = EnumSet.noneOf(ReservedAddress.class);
        for (ReservedAddress reservedAddress : values()) {
            if (reservedAddress.isInitialFundDistribution()) {
                initialFundDistributionAddresses.add(reservedAddress);
            }
        }
        return initialFundDistributionAddresses;
    }
}
