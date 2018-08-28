package io.coti.common.services;

import io.coti.common.communication.Channel;
import io.coti.common.data.NodeType;
import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.communication.interfaces.IPropagationSubscriber;
import io.coti.common.communication.interfaces.IReceiver;
import io.coti.common.communication.interfaces.ISender;
import io.coti.common.data.AddressData;
import io.coti.common.data.DspConsensusResult;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class CommunicationService {
    @Autowired
    protected IReceiver receiver;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ISender sender;
    @Autowired
    private BaseNodeAddressService addressService;
    @Autowired
    private BaseNodeTransactionService transactionService;
    @Autowired
    private BaseNodeDspVoteService dspVoteService;

    public void initSubscriber(List<String> propagationServerAddresses, NodeType nodeType) {
        HashMap<String, Consumer<Object>> classNameToSubscriberHandlerMapping = new HashMap<>();
        classNameToSubscriberHandlerMapping.put(Channel.getChannelString(TransactionData.class, nodeType), data ->
                transactionService.handlePropagatedTransaction((TransactionData) data));
        classNameToSubscriberHandlerMapping.put(Channel.getChannelString(AddressData.class, nodeType), data ->
                addressService.handlePropagatedAddress((AddressData) data));
        classNameToSubscriberHandlerMapping.put(Channel.getChannelString(DspConsensusResult.class, nodeType), data ->
                dspVoteService.handleVoteConclusion((DspConsensusResult) data));
        propagationSubscriber.init(propagationServerAddresses, classNameToSubscriberHandlerMapping);
    }

    public void initReceiver(String receivingPort, HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping) {
        receiver.init(receivingPort, classNameToReceiverHandlerMapping);
    }

    public void initSender(List<String> receivingServerAddresses){
        sender.init(receivingServerAddresses);
    }

    public void initPropagator(String propagationPort) {
        propagationPublisher.init(propagationPort);
    }
}