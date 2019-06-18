package io.coti.basenode.services.liveview.data;

import lombok.Data;

import java.time.Instant;

@Data
public class GraphTransactionData {
    private String id;
    private String leftParent;
    private String rightParent;
    private boolean isGenesis;
    private double trustScore;
    private Integer status;
    private long tccDuration;
    private Instant attachmentTime;
    private Instant transactionConsensusUpdateTime;

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof GraphTransactionData)) {
            return false;
        }
        return id.equals(((GraphTransactionData) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
