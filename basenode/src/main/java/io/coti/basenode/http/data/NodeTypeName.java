package io.coti.basenode.http.data;

import io.coti.basenode.data.NodeType;

import java.util.EnumMap;
import java.util.Map;

public enum NodeTypeName {
    FULL_NODE(NodeType.FullNode, "fullnode"),
    DSP_NODE(NodeType.DspNode, "dspnode"),
    TRUST_SCORE_NODE(NodeType.TrustScoreNode, "trustscorenode"),
    ZERO_SPEND_SERVER(NodeType.ZeroSpendServer, "zerospendserver"),
    FINANCIAL_SERVER(NodeType.FinancialServer, "financialserver"),
    NODE_MANAGER(NodeType.NodeManager, "nodemanager"),
    HISTORY_NODE(NodeType.HistoryNode, "historynode");

    private NodeType nodeType;
    private String node;

    private static class NodeTypeNames {
        private static final Map<NodeType, NodeTypeName> nodeTypeNameMap = new EnumMap<>(NodeType.class);
    }

    NodeTypeName(NodeType nodeType, String node) {
        this.nodeType = nodeType;
        NodeTypeNames.nodeTypeNameMap.put(nodeType, this);
        this.node = node;
    }

    public String getNode() {
        return node;
    }

    public static NodeType getNodeType(String node) {
        for (NodeTypeName nodeTypeName : values()) {
            if (nodeTypeName.node.equals(node)) {
                return nodeTypeName.nodeType;
            }
        }
        throw new IllegalArgumentException("Unknown node type " + node);
    }

    public static NodeTypeName getByNodeType(NodeType nodeType) {
        NodeTypeName nodeTypeName = NodeTypeNames.nodeTypeNameMap.get(nodeType);
        if (nodeTypeName != null) {
            return nodeTypeName;
        }

        throw new IllegalArgumentException("Unknown node type " + nodeType);
    }
}
