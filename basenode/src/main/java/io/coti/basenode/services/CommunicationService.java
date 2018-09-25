package io.coti.basenode.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.interfaces.IAddressService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.ITransactionService;
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
    private IAddressService addressService;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IDspVoteService dspVoteService;

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

    public void initSender(List<String> receivingServerAddresses) {
        sender.init(receivingServerAddresses);
    }

    public void initPropagator(String propagationPort) {
        propagationPublisher.init(propagationPort);
    }
}