package io.coti.nodemanager.services.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class NodeWeighted {
    private Hash nodeHash;
    private double weight;

    public NodeWeighted(Hash nodeHash, double weight) {
        this.nodeHash = nodeHash;
        this.weight = weight;
    }
}
