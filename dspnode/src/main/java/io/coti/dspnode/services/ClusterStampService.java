package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.data.messages.StateMessageClusterStampExecutePayload;
import io.coti.basenode.data.messages.StateMessageClusterStampInitiatedPayload;
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

    private static final long MAX_CLUSTERSTAMP_TIMEOUT = 100;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private IReceiver receiver;
    @Autowired
    private VotingTimeoutService votingTimeoutService;
    @Autowired
    private ITransactionPropagationCheckService transactionPropagationCheckService;

    @Override
    public void clusterStampInitiate(StateMessage stateMessage, StateMessageClusterStampInitiatedPayload stateMessageClusterstampInitiatedPayload) {

        if (stateMessageClusterstampInitiatedPayload.getDelay() < 0 ||
                stateMessageClusterstampInitiatedPayload.getDelay() > stateMessageClusterstampInitiatedPayload.getTimeout() ||
                stateMessageClusterstampInitiatedPayload.getTimeout() > MAX_CLUSTERSTAMP_TIMEOUT) {
            log.error("Incorrect {} message parameters {}", stateMessageClusterstampInitiatedPayload.getGeneralMessageType(), stateMessageClusterstampInitiatedPayload.toString());
            return;
        }

        votingTimeoutService.scheduleEvent("CLUSTER_STUMP_INITIATE_DELAY", stateMessageClusterstampInitiatedPayload.getDelay(), this::setResendingPause);
        votingTimeoutService.scheduleEvent("CLUSTER_STUMP_INITIATE_TIMEOUT", stateMessageClusterstampInitiatedPayload.getTimeout(), null); // todo toExecuteIfFinished should be set to emergency procedure
        receiver.setMessageQueuePause();
        propagationPublisher.propagate(stateMessage, Collections.singletonList(NodeType.FullNode));
    }

    @Override
    public void clusterStampContinueWithIndex(StateMessage stateMessage) {
        votingTimeoutService.cancelEvent("CLUSTER_STUMP_INITIATE_TIMEOUT");
        if (!receiver.isMessageQueuePause()) {
            receiver.setMessageQueuePause();
        }
        propagationPublisher.propagate(stateMessage, Collections.singletonList(NodeType.FullNode));
    }

    @Override
    public void clusterStampContinueWithHash(StateMessage stateMessage) {
        if (!receiver.isMessageQueuePause()) {
            receiver.setMessageQueuePause();
        }
        propagationPublisher.propagate(stateMessage, Collections.singletonList(NodeType.FullNode));
    }

    private void setResendingPause(String s) {
        transactionPropagationCheckService.setResendingPause();
    }

    @Override
    public void clusterStampExecute(StateMessage stateMessage, StateMessageClusterStampExecutePayload stateMessageClusterStampExecutePayload) {
        propagationPublisher.propagate(stateMessage, Collections.singletonList(NodeType.FullNode));
    }
}
