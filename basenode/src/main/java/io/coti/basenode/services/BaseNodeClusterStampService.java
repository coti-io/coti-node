package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.ClusterStampConsensusResultCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.ClusterStamps;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * An abstract class that provides basic Cluster Stamp functionality for all nodes that take part in cluster stamp flow.
 */
@Slf4j
public abstract class BaseNodeClusterStampService implements IClusterStampService {

    @Autowired
    protected IPropagationPublisher propagationPublisher;
    @Autowired
    protected ClusterStampConsensusResultCrypto clusterStampConsensusResultCrypto;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    protected TccConfirmationService tccConfirmationService;
    @Autowired
    protected Transactions transactions;
    @Autowired
    protected ClusterStamps clusterStamps;

    protected ClusterStampState clusterStampState;

    @Override
    public void init() {
        clusterStampState = ClusterStampState.OFF;
    }

    protected Map<Hash, TransactionData> getUnconfirmedTransactions() {

        Set<Hash> unreachedDspcHashTransactions = getUnreachedDspcHashTransactions();
        Set<Hash> unreachedTccHashTransactions = tccConfirmationService.getHashToTccUnConfirmTransactionsMapping().keySet();

        List<Hash> unconfirmedHashTransactions = new ArrayList<>();
        unconfirmedHashTransactions.addAll(unreachedDspcHashTransactions);
        unconfirmedHashTransactions.addAll(unreachedTccHashTransactions);

        TransactionData transactionData;
        Map<Hash, TransactionData>  unconfirmedTransactions = new HashMap<>();
        for(Hash unconfirmedHashTransaction : unconfirmedHashTransactions) {
            transactionData =  transactions.getByHash(unconfirmedHashTransaction);
            if (!transactionData.isZeroSpend()) {
                unconfirmedTransactions.put(unconfirmedHashTransaction, transactionData);
            }
        }

        return unconfirmedTransactions;
    }

    public abstract Set<Hash> getUnreachedDspcHashTransactions();

    // TODO: implement
    public void handleClusterStampData(ClusterStampData clusterStampData) {

    }

    public void handleClusterStampConsensusResult(ClusterStampConsensusResult clusterStampConsensusResult) {
        //TODO 3/4/2019 astolia: see if can make things more efficient here.
        if(clusterStampConsensusResultCrypto.verifySignature(clusterStampConsensusResult) && clusterStampConsensusResult.isDspConsensus()) {
            ClusterStampData clusterStampData = clusterStamps.getByHash(clusterStampConsensusResult.getHash());
            clusterStampData.setClusterStampConsensusResult(clusterStampConsensusResult);
            clusterStamps.deleteByHash(clusterStampConsensusResult.getHash());
            // TODO: think about hash that cluster stamp should be saved with
            clusterStampData.setHash(new Hash(""));
            //TODO 3/6/2019 astolia: seems we are saving this twice for DSP as it was saved before without consensus.
            clusterStamps.put(clusterStampData);
            log.info("Cluster stmap is saved");
            loadBalanceFromClusterStamp(clusterStampData);
        }
    }

    public void setHash(ClusterStampData clusterStampData) {
        byte[] balanceMapBytes = clusterStampData.getBalanceMap().toString().getBytes();
        byte[] unconfirmedTransactionHashesBytes = clusterStampData.getUnconfirmedTransactions().keySet().toString().getBytes();
        int byteBufferLength = balanceMapBytes.length + unconfirmedTransactionHashesBytes.length;
        ByteBuffer hashBytesBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(balanceMapBytes)
                .put(unconfirmedTransactionHashesBytes);

        clusterStampData.setHash(new Hash(hashBytesBuffer.array()));
    }

    @Override
    public void loadBalanceFromClusterStamp(ClusterStampData clusterStampData) {

        balanceService.updateBalanceAndPreBalanceMap(clusterStampData.getBalanceMap());
        // TODO: fix this
        //transactions.deleteAll();
        Iterator it = clusterStampData.getUnconfirmedTransactions().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry unConfirmedTransaction = (Map.Entry)it.next();
            transactions.put( (TransactionData)unConfirmedTransaction.getValue() );
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    @Override
    public ClusterStampData getLastClusterStamp(long totalConfirmedTransactionsPriorClusterStamp) {

        // TODO: think about hash that cluster stamp should be saved with
        ClusterStampData currentClusterStampData = clusterStamps.getByHash(new Hash("00"));
        if(currentClusterStampData != null &&
                currentClusterStampData.getTotalConfirmedTransactionsPriorClusterStamp() > totalConfirmedTransactionsPriorClusterStamp) {
            return currentClusterStampData;
        }
        return null;
    }

    @Override
    public boolean isClusterStampOff() {
        return verifyState(ClusterStampState.OFF);
    }

    @Override
    public boolean isClusterStampInProcess(){
        return verifyState(ClusterStampState.IN_PROCESS);
    }

    @Override
    public boolean isPreparingForClusterStamp() {
        return verifyState(ClusterStampState.PREPARING);
    }

    @Override
    public boolean isReadyForClusterStamp(){
        return verifyState(ClusterStampState.READY);
    }

    private boolean verifyState(ClusterStampState expectedState){
        return clusterStampState == expectedState;
    }
}
