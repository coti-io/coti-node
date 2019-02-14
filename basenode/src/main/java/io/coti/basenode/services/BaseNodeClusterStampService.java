package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.ClusterStampConsensusResultCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.ClusterStamp;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IDspVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;

@Slf4j
@Service
public class BaseNodeClusterStampService {

    @Autowired
    protected IPropagationPublisher propagationPublisher;
    @Autowired
    protected ClusterStampConsensusResultCrypto clusterStampConsensusResultCrypto;
    @Autowired
    protected IDspVoteService dspVoteService;
    @Autowired
    protected TccConfirmationService tccConfirmationService;
    @Autowired
    protected Transactions transactions;
    @Autowired
    protected ClusterStamp clusterStamp;

    protected boolean isReadyForClusterStamp;

    public boolean isReadyForClusterStamp() {
        return isReadyForClusterStamp;
    }

    protected Map<Hash, TransactionData> getUnconfirmedTransactions() {

        Set unreachedDspcHashTransactions = dspVoteService.getTransactionHashToVotesListMapping().keySet();
        Set unreachedTccHashTransactions = tccConfirmationService.getHashToTccUnConfirmTransactionsMapping().keySet();

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

    public void newClusterStampConsensusResult(ClusterStampConsensusResult clusterStampConsensusResult) {

        if(clusterStampConsensusResultCrypto.verifySignature(clusterStampConsensusResult) && clusterStampConsensusResult.isDspConsensus()) {

            ClusterStampData clusterStampData = clusterStamp.getByHash(clusterStampConsensusResult.getHash());
            clusterStampData.setClusterStampConsensusResult(clusterStampConsensusResult);
            clusterStamp.put(clusterStampData);

            isReadyForClusterStamp = false;
            transactions.deleteAll();
            Iterator it = clusterStampData.getUnconfirmedTransactions().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry unConfirmedTransaction = (Map.Entry)it.next();
                transactions.put( (TransactionData)unConfirmedTransaction.getValue() );
                it.remove(); // avoids a ConcurrentModificationException
            }
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

}
