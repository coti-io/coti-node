package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService implements IClusterStampService {

    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ISender sender;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private IValidationService validationService;
    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
    private List<TransactionData> clusterStampTransactions;
    private ClusterStampState clusterStampState;

    @PostConstruct
    @Override
    public void init(){
        clusterStampState = ClusterStampState.OFF;
        clusterStampTransactions = new ArrayList<>();
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
        if(validationService.validatePrepareForClusterStampRequest(clusterStampPreparationData, clusterStampState)){
            log.debug("Start preparation of Full node for cluster stamp");
            clusterStampState = clusterStampState.nextState();

            FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData = new FullNodeReadyForClusterStampData(clusterStampPreparationData.getTotalConfirmedTransactionsCount());
            clusterStampStateCrypto.signMessage(fullNodeReadyForClusterStampData);
            receivingServerAddresses.forEach(address -> sender.send(fullNodeReadyForClusterStampData, address));
        }
    }

    @Override
    public boolean isClusterStampReady(){

        return clusterStampState == ClusterStampState.READY;
    }

    @Override
    public boolean isClusterStampInProcess(){

        return clusterStampState == ClusterStampState.IN_PROCESS;
    }

    @Override
    public boolean isClusterStampPreparing() {

        return clusterStampState == ClusterStampState.PREPARING;
    }

    public void handleDspReadyForClusterStampData(DspReadyForClusterStampData dspReadyForClusterStampData) {
        if(clusterStampStateCrypto.verifySignature(dspReadyForClusterStampData)) {
            clusterStampState = clusterStampState.nextState();
        }
    }

    public void handleClusterStamp(ClusterStampData clusterStampData) {

        if(clusterStampCrypto.verifySignature(clusterStampData)) {
            clusterStamps.put(clusterStampData);
        }
    }

    public void addClusterStampTransaction(TransactionData clusterStampTransaction){
        clusterStampTransactions.add(clusterStampTransaction);
    }

    @Override
    public void handleClusterStampConsensusResult(ClusterStampConsensusResult clusterStampConsensusResult) {
        super.handleClusterStampConsensusResult(clusterStampConsensusResult);
        if(!clusterStampTransactions.isEmpty()){
            clusterStampTransactions.forEach( transaction -> {
                handleUnfinishedClusterStampTransaction(transaction);
                clusterStampTransactions.remove(transaction);
            });
        }
    }

    private void handleUnfinishedClusterStampTransaction(TransactionData transactionData){
        final TransactionData finalTransactionData = transactionData;
        receivingServerAddresses.forEach(address -> sender.send(finalTransactionData, address));
        transactionHelper.setTransactionStateToFinished(transactionData);
    }
}