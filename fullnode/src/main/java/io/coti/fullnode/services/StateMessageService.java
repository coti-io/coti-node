package io.coti.fullnode.services;

import io.coti.basenode.crypto.GeneralMessageCrypto;
import io.coti.basenode.data.messages.GeneralMessageType;
import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.services.BaseNodeStateMessageService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StateMessageService extends BaseNodeStateMessageService {

    @Autowired
    private INetworkService networkService;
    @Autowired
    private TransactionPropagationCheckService transactionPropagationCheckService;
    @Autowired
    private GeneralMessageCrypto generalMessageCrypto;

    @Override
    public void continueHandleStateMessage(StateMessage stateMessage) {
        if (incorrectMessageSender(stateMessage)) {
            return;
        }
        if (incorrectMessageSenderSignature(stateMessage)) {
            return;
        }
        if (stateMessage.getMessagePayload().getGeneralMessageType() == GeneralMessageType.CLUSTER_STAMP_INITIATED) {
            transactionPropagationCheckService.setPauseResending(true);  // todo restart it
        }
    }
}
