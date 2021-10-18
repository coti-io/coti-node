package io.coti.basenode.services;

import io.coti.basenode.data.NodeType;

import java.util.*;

public enum NodeTypeService {
    FULL_NODE(NodeType.FullNode, true),
    DSP_NODE(NodeType.DspNode, true),
    TRUST_SCORE_NODE(NodeType.TrustScoreNode, true),
    ZERO_SPEND_SERVER(NodeType.ZeroSpendServer, false),
    FINANCIAL_SERVER(NodeType.FinancialServer, false),
    HISTORY_NODE(NodeType.HistoryNode, true);

    private final NodeType nodeType;
    private final boolean multipleNode;

    private static class NodeTypeServices {
        private static final Map<NodeType, NodeTypeService> nodeTypeServiceMap = new EnumMap<>(NodeType.class);
    }

    NodeTypeService(NodeType nodeType, boolean multipleNode) {
        this.nodeType = nodeType;
        NodeTypeServices.nodeTypeServiceMap.put(nodeType, this);
        this.multipleNode = multipleNode;
    }

    public static NodeTypeService getByNodeType(NodeType nodeType) {
        return NodeTypeServices.nodeTypeServiceMap.get(nodeType);
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
