package io.coti.financialserver.data;

public enum DistributionEntryStatus {

    ONHOLD(0),
    ACCEPTED(1),
    FAILED(2),
    CREATED(3),
    CANCELLED(4);

    private long index;

    DistributionEntryStatus(long index) {
        this.index = index;
    }
}
