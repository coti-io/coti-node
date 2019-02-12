package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.*;
import io.coti.basenode.model.ClusterStamp;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.TccConfirmationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    final private static int DSP_NODES_MAJORITY = 1;

    private boolean isClusterStampInMaking;
    private ClusterStampData clusterStampInProcess;

    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ClusterStamp clusterStamp;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private SourceStarvationService sourceStarvationService;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private TccConfirmationService tccConfirmationService;
    @Autowired
    private Transactions transactions;

    @PostConstruct
    private void init() {
        isClusterStampInMaking = false;
        clusterStampInProcess = new ClusterStampData(new Hash("inProgress"));
    }

    @Override
    public void dspNodeReadyForClusterStamp(DspReadyForClusterStampData dspReadyForClusterStampData) {

        log.debug("Ready for cluster stamp propagated message received from DSP to ZS");

        clusterStampInProcess.getDspReadyForClusterStampDataList().add(dspReadyForClusterStampData);

        if ( clusterStampInProcess.getDspReadyForClusterStampDataList().size() >= DSP_NODES_MAJORITY ) {
            log.info("Stop dsp vote service from sum and save dsp votes");
            dspVoteService.stopSumAndSaveVotes();
            sourceStarvationService.stopCheckSourcesStarvation();
        }
    }

    public void makeAndPropagateClusterStamp() {

        ClusterStampData clusterStampData = clusterStampInProcess;
        clusterStampData.setBalanceMap(balanceService.getBalanceMap());
        clusterStampData.setUnconfirmedTransactions(getUnconfirmedTransactions());
        clusterStampData.setHash();
        propagationPublisher.propagate(clusterStampData, Arrays.asList(NodeType.DspNode));

        clusterStampInProcess = new ClusterStampData(new Hash("inProgress"));
        log.info("Restart DSP vote service to sum and save dsp votes, and starvation service");
        //dspVoteService.startSumAndSaveVotes();
        //sourceStarvationService.startCheckSourcesStarvation();
    }

    public boolean getIsClusterStampInMaking() {
        return isClusterStampInMaking;
    }

    private List<TransactionData> getUnconfirmedTransactions() {

        Set unreachedDspcHashTransactions = dspVoteService.getTransactionHashToVotesListMapping().keySet();
        Set unreachedTccHashTransactions = tccConfirmationService.getHashToTccUnConfirmTransactionsMapping().keySet();

        List<Hash> unconfirmedHashTransactions = new ArrayList<>();
        unconfirmedHashTransactions.addAll(unreachedDspcHashTransactions);
        unconfirmedHashTransactions.addAll(unreachedTccHashTransactions);

        List<TransactionData> unconfirmedTransactions = new ArrayList<>();
        for(Hash unconfirmedHashTransaction : unconfirmedHashTransactions) {
            unconfirmedTransactions.add(transactions.getByHash(unconfirmedHashTransaction));
        }

        return unconfirmedTransactions;
    }
}