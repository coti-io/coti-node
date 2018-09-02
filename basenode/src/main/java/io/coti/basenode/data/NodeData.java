package io.coti.basenode.data;

import lombok.Data;

import java.util.Date;

@Data
public class NodeData {
    private String id;
    private String leftParent;
    private String rightParent;
    private boolean isGenesis;
    private double trustScore;
    private Integer status;
    private long tccDuration;
    private Date attachmentTime;
    private Date transactionConsensusUpdateTime;

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof NodeData)) {
            return false;
        }
        return id.equals(((NodeData) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
