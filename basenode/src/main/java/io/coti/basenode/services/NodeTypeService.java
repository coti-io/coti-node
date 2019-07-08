package io.coti.basenode.services;

import io.coti.basenode.data.NodeType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public enum NodeTypeService {
    FullNode(NodeType.FullNode, true),
    DspNode(NodeType.DspNode, true),
    TrustScoreNode(NodeType.TrustScoreNode, true),
    ZeroSpendServer(NodeType.ZeroSpendServer, false),
    FinancialServer(NodeType.FinancialServer, false),
    HistoryNode(NodeType.HistoryNode, true);

    private NodeType nodeType;
    private boolean multipleNode;

    NodeTypeService(NodeType nodeType, boolean multipleNode) {
        this.nodeType = nodeType;
        this.multipleNode = multipleNode;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public boolean isMultipleNode() {
        return multipleNode;
    }

    public static List<NodeType> getNodeTypeList(boolean multipleNode) {
        List<NodeType> nodeTypes = new ArrayList<>();
        EnumSet.allOf(NodeTypeService.class).forEach(nodeTypeService -> {
            if (nodeTypeService.multipleNode == multipleNode) {
                nodeTypes.add(nodeTypeService.nodeType);
            }
        });
        return nodeTypes;
    }
}
