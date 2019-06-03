package io.coti.financialserver.data;

public enum FundDistributionEntry {
    ID(0),
    RECEIVER_ADDRESS(1),
    DISTRIBUTION_POOL(2),
    AMOUNT(3),
    CREATION_TIME(4),
    RELEASE_TIME(5),
    SOURCE(6),
    SIGNATURE_R(0),
    SIGNATURE_S(1);

    private int index;

    FundDistributionEntry(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
