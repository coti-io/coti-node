package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.TransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler for PrepareForSnapshot messages propagated to FullNode.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;
    @Autowired
    private ISender sender;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Autowired
    TransactionHelper transactionHelper;

    private List<TransactionData> clusterStampTransactions;

    @PostConstruct
    protected void init(){
        super.init();
        clusterStampTransactions = new ArrayList<>();
    }

    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {

        log.debug("Prepare for cluster stamp propagated message received from DSP to FN");

        if(!amIReadyForClusterStamp && clusterStampStateCrypto.verifySignature(clusterStampPreparationData)) {
            FullNodeReadyForClusterStampData fullNodeReadyForClusterStampData = new FullNodeReadyForClusterStampData(clusterStampPreparationData.getTotalConfirmedTransactionsCount());
            clusterStampStateCrypto.signMessage(fullNodeReadyForClusterStampData);
            receivingServerAddresses.forEach(address -> sender.send(fullNodeReadyForClusterStampData, address));
            amIReadyForClusterStamp = true;
        }
        else {
            log.info("Full Node is already preparing for cluster stamp");
            //TODO 2/4/2019 astolia: Send to DSP that snapshot prepare is in process?
        }
    }

    public void handleDspReadyForClusterStampData(DspReadyForClusterStampData dspReadyForClusterStampData) {
        if(clusterStampStateCrypto.verifySignature(dspReadyForClusterStampData)) {
            isMyParentNodeReadyForClusterStamp = true;
        }
    }

    public void newClusterStamp(ClusterStampData clusterStampData) {

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