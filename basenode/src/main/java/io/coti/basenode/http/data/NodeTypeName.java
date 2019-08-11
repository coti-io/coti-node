package io.coti.basenode.http.data;

import io.coti.basenode.data.NodeType;

public enum NodeTypeName {
    FullNode(NodeType.FullNode, "fullnode"),
    DspNode(NodeType.DspNode, "dspnode"),
    TrustScoreNode(NodeType.TrustScoreNode, "trustscorenode"),
    ZeroSpendServer(NodeType.ZeroSpendServer, "zerospendserver"),
    FinancialServer(NodeType.FinancialServer, "financialserver"),
    NodeManager(NodeType.NodeManager, "nodemanager"),
    HistoryNode(NodeType.HistoryNode, "historynode");

    private NodeType nodeType;
    private String node;

    private NodeTypeName(NodeType nodeType, String node) {
        this.nodeType = nodeType;
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
}
