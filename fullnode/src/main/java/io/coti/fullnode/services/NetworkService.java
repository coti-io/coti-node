package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.TransactionSyncException;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.ITransactionSynchronizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NetworkService extends BaseNodeNetworkService {

    @Autowired
    private ISender sender;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private ITransactionSynchronizationService transactionSynchronizationService;

    private List<NetworkNodeData> connectedDspNodes = new ArrayList<>(2);

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        super.handleNetworkChanges(newNetworkData);

        Map<Hash, NetworkNodeData> newDspNodeMap = newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode);

        handleConnectedDspNodesChange(connectedDspNodes, newDspNodeMap, NodeType.FullNode);

        if (connectedDspNodes.size() < 2) {
            List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newDspNodeMap.values(), connectedDspNodes));
            Collections.shuffle(dspNodesToConnect);
            for (int i = 0; i < dspNodesToConnect.size() && i < 2 - connectedDspNodes.size(); i++) {
                if (i == 0 && recoveryServerAddress == null) {
                    recoveryServerAddress = dspNodesToConnect.get(0).getHttpFullAddress();
                }
                communicationService.addSubscription(dspNodesToConnect.get(i).getPropagationFullAddress(), NodeType.DspNode);
                communicationService.addSender(dspNodesToConnect.get(i).getReceivingFullAddress());
                connectedDspNodes.add(dspNodesToConnect.get(i));
            }
        }

        setNetworkData(newNetworkData);
    }

    public void addToConnectedDspNodes(NetworkNodeData networkNodeData) {
        connectedDspNodes.add(networkNodeData);
    }

    public void sendDataToConnectedDspNodes(IPropagatable propagatable) {
        connectedDspNodes.forEach(networkNodeData -> sender.send(propagatable, networkNodeData.getReceivingFullAddress()));
    }

    @Override
    public boolean isNodeConnectedToNetwork(NetworkData newNetworkData) {
        return newNetworkData.getMultipleNodeMaps().get(NodeType.FullNode).get(networkNodeData.getNodeHash()) != null;
    }

    @Override
    public synchronized void recoveryOnReconnect(String publisherAddressAndPort, NodeType publisherNodeType) {
        if (publisherNodeType != NodeType.DspNode) {
            return;
        }
        boolean ifNeedRecovery = false;

        NetworkNodeData recoveryServerNetworkNodeData = multipleNodeMaps.get(NodeType.DspNode).values().stream().filter(networkNode ->
                networkNode.getHttpFullAddress().equals(recoveryServerAddress)).findFirst().orElse(null);
        String oldRecoveryServerAddress = recoveryServerAddress;
        if (recoveryServerNetworkNodeData == null) {
            List<NetworkNodeData> dspNetworkNodeData = getShuffledNetworkNodeDataListFromMapValues(NodeType.DspNode);
            if (!dspNetworkNodeData.isEmpty()) {
                recoveryServerNetworkNodeData = dspNetworkNodeData.get(0);
                setRecoveryServerAddress(recoveryServerNetworkNodeData.getHttpFullAddress());
                ifNeedRecovery = !recoveryServerAddress.equals(oldRecoveryServerAddress);
            }
        }
//        ifNeedRecovery = ifNeedRecovery || recoveryServerNetworkNodeData == null
//                                        || publisherAddressAndPort.equals(recoveryServerNetworkNodeData.getPropagationFullAddress());

        if (ifNeedRecovery && recoveryServerAddress != null && transactionIndexService.getLastTransactionIndexData() != null) {
            try {
                transactionSynchronizationService.requestMissingTransactions(transactionIndexService.getLastTransactionIndexData().getIndex() + 1);
            } catch (TransactionSyncException e) {
                recoveryServerAddress = null;
            }
        }

    }

}
