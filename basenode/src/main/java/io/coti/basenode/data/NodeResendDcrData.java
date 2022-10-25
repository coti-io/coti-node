package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

@Data
public class NodeResendDcrData implements IPropagatable {
    private static final long serialVersionUID = 5132882274180901571L;
    private Hash nodeHash;
    private NodeType nodeType;
    private Long firstMissedIndex;
    private Long inRangeLastMissedIndex;

    public NodeResendDcrData() {
    }

    public NodeResendDcrData(Hash nodeHash, NodeType nodeType, Long firstMissedIndex, Long inRangeLastMissedIndex) {
        this.nodeHash = nodeHash;
        this.nodeType = nodeType;
        this.firstMissedIndex = firstMissedIndex;
        this.inRangeLastMissedIndex = inRangeLastMissedIndex;
    }

    @Override
    public Hash getHash() {
        return nodeHash;
    }

    @Override
    public void setHash(Hash hash) {
        nodeHash = hash;
    }
}
