package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A service that provides Cluster Stamp functionality for Full node.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

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

    @PostConstruct
    @Override
    public void init(){
        super.init();
        clusterStampTransactions = new ArrayList<>();
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
        if(validationService.validateRequestAndOffState(clusterStampPreparationData, clusterStampState)){
            log.debug("Start preparation of Full node for cluster stamp");
            clusterStampState = clusterStampState.nextState(clusterStampPreparationData); //Change state to PREPARING

            FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData = new FullNodeReadyForClusterStampData(clusterStampPreparationData.getTotalConfirmedTransactionsCount());
            clusterStampStateCrypto.signMessage(fullNodeReadyForClusterStampData);
            receivingServerAddresses.forEach(address -> sender.send(fullNodeReadyForClusterStampData, address));
        }
    }

    @Override
    public void getReadyForClusterStamp(ClusterStampStateData dspReadyForClusterStampData) {
        if(validationService.validateRequestAndPreparingState(dspReadyForClusterStampData,clusterStampState)) {
            clusterStampState = clusterStampState.nextState((DspReadyForClusterStampData) dspReadyForClusterStampData); //Change state to READY
        }
    }


    public void handleClusterStamp(ClusterStampData clusterStampData) {

        if(clusterStampCrypto.verifySignature(clusterStampData)) {
            clusterStampState = clusterStampState.nextState(clusterStampData); //Change state to IN_PROCESS
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
            clusterStampTransactions.forEach(this::handleUnfinishedClusterStampTransaction);
        }
        clusterStampState = clusterStampState.nextState(clusterStampConsensusResult); //Change state to OFF
    }

    private void handleUnfinishedClusterStampTransaction(TransactionData transactionData){
        final TransactionData finalTransactionData = transactionData;
        receivingServerAddresses.forEach(address -> sender.send(finalTransactionData, address));
        transactionHelper.setTransactionStateToFinished(transactionData);
        clusterStampTransactions.remove(transactionData);
    }

    @Override
    public Set<Hash> getUnreachedDspcHashTransactions(){
        throw new UnsupportedOperationException();
    }
}