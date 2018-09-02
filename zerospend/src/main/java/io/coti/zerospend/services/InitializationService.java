package io.coti.zerospend.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.data.*;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Service
public class InitializationService {
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;
    @Value("${propagation.port}")
    private String propagationPort;

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;

    @Autowired
    private ZeroSpendTransactionCreationService zeroSpendService;


    @Autowired
    private TransactionIndexService transactionIndexes;

    @Autowired
    private ZeroSpendTransactionCreationService zeroSpendTransactionCreationService;


    @PostConstruct
    public void init() {
        baseNodeInitializationService.init();


        initDataBase();

        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(DspVote.class.getName(), data ->
                dspVoteService.receiveDspVote((DspVote) data));
        classNameToReceiverHandlerMapping.put(Channel.getChannelString(ZeroSpendTransactionRequest.class, NodeType.ZeroSpendServer) , newZeroSpendTransactionRequest -> zeroSpendTransactionCreationService.createNewGenesisZeroSpendTransaction((ZeroSpendTransactionRequest) newZeroSpendTransactionRequest));
        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSubscriber(propagationServerAddresses, NodeType.ZeroSpendServer);
        communicationService.initPropagator(propagationPort);

    }



    private void initDataBase(){

        zeroSpendService.setGenesisTransactions();

    }
}