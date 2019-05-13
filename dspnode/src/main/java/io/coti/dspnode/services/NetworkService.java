package io.coti.dspnode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeNetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NetworkService extends BaseNodeNetworkService {

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        super.handleNetworkChanges(newNetworkData);

        Map<Hash, NetworkNodeData> newDspNodeMap = newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode);
        List<NetworkNodeData> connectedDspNodes = getMapFromFactory(NodeType.DspNode).values().stream()
                .filter(dspNode -> !dspNode.equals(networkNodeData))
                .collect(Collectors.toList());
        handleConnectedDspNodesChange(connectedDspNodes, newDspNodeMap, NodeType.DspNode);

        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode).values(),
                connectedDspNodes));
        dspNodesToConnect.removeIf(dspNode -> dspNode.equals(networkNodeData));
        addListToSubscription(dspNodesToConnect);

        handleConnectedSingleNodeChange(newNetworkData, NodeType.ZeroSpendServer, NodeType.DspNode);
        handleConnectedSingleNodeChange(newNetworkData, NodeType.FinancialServer, NodeType.DspNode);

        setNetworkData(newNetworkData);
    }

    @Override
    public boolean isNodeConnectedToNetwork(NetworkData newNetworkData) {
        return newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode).get(networkNodeData.getNodeHash()) != null;
    }

    public List<MessageArrivalValidationData> sendDataToConnectedNodeByHttp(MessageArrivalValidationData data) {
        RestTemplate restTemplate = new RestTemplate();
        List<MessageArrivalValidationData> validatedNotReceived = new ArrayList<>();
        NetworkNodeData zeroSpend = super.singleNodeNetworkDataMap.get(NodeType.ZeroSpendServer);
        validatedNotReceived.add(sendMessageArrivalValidationDataToConnectedDsp(restTemplate, buildMessageArrivalValidationHttpUrl(zeroSpend), data));
        return validatedNotReceived;
    }

    private String buildMessageArrivalValidationHttpUrl(NetworkNodeData networkNodeData){
        StringBuilder sb = new StringBuilder();
        sb.append("http://").
                append(networkNodeData.getAddress()).
                append(":").
                append(networkNodeData.getHttpPort()).append("/missedDataHashes");
        return sb.toString();
    }

    private MessageArrivalValidationData sendMessageArrivalValidationDataToConnectedDsp(RestTemplate restTemplate, String uri, MessageArrivalValidationData data){
        try {
            ResponseEntity<MessageArrivalValidationData> response =
                    restTemplate.postForEntity(
                            uri,
                            data,
                            MessageArrivalValidationData.class);
            return response.getBody();
        }catch (Exception e){
            log.info(e.getMessage());
        }
        return new MessageArrivalValidationData();
    }
}
