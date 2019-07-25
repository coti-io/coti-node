package io.coti.historynode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.Instant;

@Data
public class AddressMissingTransactionsByHash implements IEntity {

    private Hash hash;
    private Instant lastTimeEncountered;
    private String status; //TODO 7/25/2019 tomer: Consider changing to an enum
    private Instant lastTimeUpdatedStatus;

    public AddressMissingTransactionsByHash(Hash hash, Instant lastTimeEncountered, String status, Instant lastTimeUpdatedStatus) {
        this.hash = hash;
        this.lastTimeEncountered = lastTimeEncountered;
        this.status = status;
        this.lastTimeUpdatedStatus = lastTimeUpdatedStatus;
    }
}
