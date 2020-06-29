package io.coti.basenode.services;

import io.coti.basenode.crypto.GeneralMessageCrypto;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.messages.GeneralMessageType;
import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.services.interfaces.IGeneralVoteService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.IStateMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

@Slf4j
@Service
public class BaseNodeStateMessageService implements IStateMessageService {

    @Autowired
    private INetworkService networkService;
    @Autowired
    private GeneralMessageCrypto generalMessageCrypto;
    @Autowired
    protected IGeneralVoteService generalVoteService;

    private EnumMap<NodeType, List<GeneralMessageType>> publisherNodeTypeToGeneralMessageTypesMap = new EnumMap<>(NodeType.class);

    public void init() {
        publisherNodeTypeToGeneralMessageTypesMap.put(NodeType.ZeroSpendServer,
                Arrays.asList(GeneralMessageType.CLUSTER_STAMP_INITIATED,
                        GeneralMessageType.CLUSTER_STAMP_PREPARE_INDEX,
                        GeneralMessageType.CLUSTER_STAMP_PREPARE_HASH,
                        GeneralMessageType.CLUSTER_STAMP_CONTINUE,
                        GeneralMessageType.CLUSTER_STAMP_EXECUTE));
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void handleStateMessage(StateMessage stateMessage) {
        log.info("State message received: " + stateMessage.getMessagePayload().getGeneralMessageType().toString());
        if (incorrectMessageSender(stateMessage)) {
            return;
        }
        if (incorrectMessageSenderSignature(stateMessage)) {
            return;
        }
        continueHandleStateMessage(stateMessage);
    }

    protected void continueHandleStateMessage(StateMessage stateMessage) {
        // implemented in subclasses
    }

    protected boolean incorrectMessageSender(StateMessage stateMessage) {
        NodeType nodeType = networkService.getNetworkNodeType(stateMessage.getSignerHash());
        return !publisherNodeTypeToGeneralMessageTypesMap.containsKey(nodeType) ||
                !publisherNodeTypeToGeneralMessageTypesMap.get(nodeType).contains(stateMessage.getMessagePayload().getGeneralMessageType());
    }

    protected boolean incorrectMessageSenderSignature(StateMessage stateMessage) {
        if (!generalMessageCrypto.verifySignature(stateMessage)) {
            log.error("State message signature verification failed: {}", stateMessage);
            return true;
        }
        return false;
    }
}
