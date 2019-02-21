package io.coti.historynode.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.data.ClusterStampConsensusResult;
import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Service
public class HistoryInitializationService {
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;

    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;
    @Autowired
    private CommunicationService communicationService;
    @Autowired


    private ClusterStampService clusterStampService;

    @PostConstruct
    public void init() {
        initSubscriber();
        baseNodeInitializationService.init();
    }

    public void initSubscriber() {
        HashMap<String, Consumer<Object>> classNameToSubscriberHandler = new HashMap<>();

        classNameToSubscriberHandler.put(Channel.getChannelString(ClusterStampData.class, NodeType.HistoryNode), data ->
                clusterStampService.newClusterStamp((ClusterStampData) data));
        classNameToSubscriberHandler.put(Channel.getChannelString(ClusterStampConsensusResult.class, NodeType.HistoryNode), data ->
                clusterStampService.handleClusterStampConsensusResult((ClusterStampConsensusResult) data));

        communicationService.initSubscriber(propagationServerAddresses, NodeType.HistoryNode, classNameToSubscriberHandler);
    }
}
