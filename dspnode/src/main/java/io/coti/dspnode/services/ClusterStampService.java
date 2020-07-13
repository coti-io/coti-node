package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.data.ClusterStampNameData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.messages.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.VotingTimeoutService;
import io.coti.basenode.services.interfaces.ITransactionPropagationCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private IReceiver receiver;
    @Autowired
    private VotingTimeoutService votingTimeoutService;
    @Autowired
    private ITransactionPropagationCheckService transactionPropagationCheckService;

    @Override
    public void clusterStampInitiate(StateMessageData stateMessage, InitiateClusterStampStateMessageData initiateClusterStampStateMessageData) {

        if (initiateClusterStampStateMessageData.getDelay() < 0 ||
                initiateClusterStampStateMessageData.getDelay() > initiateClusterStampStateMessageData.getTimeout() ||
                initiateClusterStampStateMessageData.getTimeout() > CLUSTER_STAMP_TIMEOUT) {
            log.error("Incorrect {} message parameters {}", StateMessageType.getName(initiateClusterStampStateMessageData.getClass()), initiateClusterStampStateMessageData.toString());
            return;
        }

        clusterStampInitiateTimestamp = stateMessage.getCreateTime();
        votingTimeoutService.scheduleEvent("CLUSTER_STUMP_INITIATE_DELAY", initiateClusterStampStateMessageData.getDelay(), this::setResendingPause);
        votingTimeoutService.scheduleEvent("CLUSTER_STUMP_INITIATE_TIMEOUT", initiateClusterStampStateMessageData.getTimeout(), null); // todo toExecuteIfFinished should be set to emergency procedure
        receiver.setMessageQueuePause();
        propagationPublisher.propagate(stateMessage, Collections.singletonList(NodeType.FullNode));
    }

    @Override
    public void clusterStampContinueWithIndex(LastIndexClusterStampStateMessageData lastIndexClusterStampStateMessageData) {
        super.clusterStampContinueWithIndex(lastIndexClusterStampStateMessageData);
        votingTimeoutService.cancelEvent("CLUSTER_STUMP_INITIATE_TIMEOUT");  // todo reset it instead of cancelling
        if (!receiver.isMessageQueuePause()) {
            receiver.setMessageQueuePause();
        }
        propagationPublisher.propagate(lastIndexClusterStampStateMessageData, Collections.singletonList(NodeType.FullNode));
    }

    @Override
    public void clusterStampContinueWithHash(StateMessageData stateMessage) {
        if (!receiver.isMessageQueuePause()) {
            receiver.setMessageQueuePause();
        }
        propagationPublisher.propagate(stateMessage, Collections.singletonList(NodeType.FullNode));
    }

    private void setResendingPause(String s) {
        transactionPropagationCheckService.setResendingPause();
    }

    @Override
    public void clusterStampExecute(ExecuteClusterStampStateMessageData executeClusterStampStateMessageData) {
        if (lastConfirmedIndexForClusterStamp != executeClusterStampStateMessageData.getLastIndex()) {
            log.error("Incorrect index in the CLUSTER_STAMP_EXECUTE message {} {}", lastConfirmedIndexForClusterStamp, executeClusterStampStateMessageData.getLastIndex());
            return;
        }
        String clusterStampCreateTimeString = String.valueOf(this.clusterStampCreateTime.toEpochMilli());
        clusterStampName = new ClusterStampNameData(clusterStampCreateTimeString, clusterStampCreateTimeString);
        propagationPublisher.propagate(executeClusterStampStateMessageData, Collections.singletonList(NodeType.FullNode));
        super.clusterStampExecute(executeClusterStampStateMessageData);
    }

    @Override
    protected void restartTransactionProcessing() {
        receiver.endMessageQueuePause();
        transactionPropagationCheckService.endResendingPause();
        votingTimeoutService.cancelEvent("CLUSTER_STUMP_INITIATE_TIMEOUT");
    }
}
