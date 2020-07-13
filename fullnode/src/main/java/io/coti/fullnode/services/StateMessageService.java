package io.coti.fullnode.services;

import io.coti.basenode.data.messages.ExecuteClusterStampStateMessageData;
import io.coti.basenode.data.messages.StateMessageData;
import io.coti.basenode.services.BaseNodeStateMessageService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionPropagationCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static io.coti.basenode.data.messages.StateMessageType.getName;

@Slf4j
@Service
public class StateMessageService extends BaseNodeStateMessageService {

    @Autowired
    private INetworkService networkService;
    @Autowired
    private ITransactionPropagationCheckService transactionPropagationCheckService;
    @Autowired
    private IClusterStampService clusterStampService;

    @Override
    public void continueHandleStateMessage(StateMessageData stateMessage) {
        switch (Objects.requireNonNull(getName(stateMessage.getClass()))) {
            case CLUSTER_STAMP_EXECUTE:
                clusterStampService.clusterStampExecute((ExecuteClusterStampStateMessageData) stateMessage);
                break;
            case CLUSTER_STAMP_INITIATED:
                transactionPropagationCheckService.setResendingPause();
                break;
            default:
                break;
        }
    }
}
