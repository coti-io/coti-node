package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.services.NodeTypeService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
public class NetworkData implements IPropagatable {

    private static final long serialVersionUID = 1145085728326369679L;
    private Map<NodeType, Map<Hash, NetworkNodeData>> multipleNodeMaps;
    private Map<NodeType, NetworkNodeData> singleNodeNetworkDataMap;

    public NetworkData() {
        multipleNodeMaps = new EnumMap<>(NodeType.class);
        NodeTypeService.getNodeTypeList(true).forEach(nodeType -> multipleNodeMaps.put(nodeType, new ConcurrentHashMap<>()));

        singleNodeNetworkDataMap = new EnumMap<>(NodeType.class);
        NodeTypeService.getNodeTypeList(false).forEach(nodeType -> singleNodeNetworkDataMap.put(nodeType, null));

    }

    @Override
    public Hash getHash() {
        return new Hash(1);
    }

    @Override
    public void setHash(Hash hash) {

    }
}