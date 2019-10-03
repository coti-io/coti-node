package io.coti.nodemanager.services.data;

import io.coti.basenode.data.NetworkNodeData;
import lombok.Data;

@Data
public class WeightedNode {

    private NetworkNodeData node;
    private double weight;

    public WeightedNode(NetworkNodeData node, double weight) {
        this.node = node;
        this.weight = weight;
    }
}
