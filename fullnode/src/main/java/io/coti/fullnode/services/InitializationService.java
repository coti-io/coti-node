package io.coti.fullnode.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.data.*;
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
public class InitializationService {
    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
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

        communicationService.initSender(receivingServerAddresses);
        initSubscriber();

        baseNodeInitializationService.init();
    }

    public void initSubscriber(){
        HashMap<String, Consumer<Object>> classNameToSubscriberHandler = new HashMap<>();

        classNameToSubscriberHandler.put(Channel.getChannelString(ClusterStampPreparationData.class, NodeType.FullNode), data ->
                clusterStampService.prepareForClusterStamp((ClusterStampPreparationData) data));

        classNameToSubscriberHandler.put(Channel.getChannelString(DspReadyForClusterStampData.class, NodeType.FullNode), data ->
                clusterStampService.handleDspReadyForClusterStampData((DspReadyForClusterStampData) data));

        classNameToSubscriberHandler.put(Channel.getChannelString(ClusterStampData.class, NodeType.FullNode), data ->
                clusterStampService.handleClusterStamp((ClusterStampData) data));

        classNameToSubscriberHandler.put(Channel.getChannelString(ClusterStampConsensusResult.class, NodeType.FullNode), data ->
                clusterStampService.handleClusterStampConsensusResult((ClusterStampConsensusResult) data));

        communicationService.initSubscriber(propagationServerAddresses, NodeType.FullNode, classNameToSubscriberHandler);
    }
}