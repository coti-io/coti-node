package io.coti.basenode.services;

import io.coti.basenode.crypto.StateMessageCrypto;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.messages.StateMessageData;
import io.coti.basenode.data.messages.StateMessageType;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.IStateMessageService;
import io.coti.basenode.services.interfaces.IVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class BaseNodeStateMessageService implements IStateMessageService {

    @Autowired
    private INetworkService networkService;
    @Autowired
    private StateMessageCrypto stateMessageCrypto;
    @Autowired
    protected IVoteService voteService;
    private final EnumMap<NodeType, List<StateMessageType>> publisherNodeTypeToStateMessageTypesMap = new EnumMap<>(NodeType.class);

    public void init() {
        publisherNodeTypeToStateMessageTypesMap.put(NodeType.ZeroSpendServer,
                Arrays.asList(StateMessageType.CLUSTER_STAMP_INITIATED,
                        StateMessageType.CLUSTER_STAMP_PREPARE_INDEX,
                        StateMessageType.CLUSTER_STAMP_PREPARE_HASH,
                        StateMessageType.CLUSTER_STAMP_CONTINUE,
                        StateMessageType.CLUSTER_STAMP_EXECUTE));
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void handleStateMessage(StateMessageData stateMessage) {
        log.info("State message received: " + Objects.requireNonNull(StateMessageType.getName(stateMessage.getClass())).toString());
        if (incorrectMessageSender(stateMessage)) {
            return;
        }
        if (incorrectMessageSenderSignature(stateMessage)) {
            return;
        }
        continueHandleStateMessage(stateMessage);
    }

    protected void continueHandleStateMessage(StateMessageData stateMessage) {
        // implemented in subclasses
    }

    protected boolean incorrectMessageSender(StateMessageData stateMessage) {
        NodeType nodeType = networkService.getNetworkNodeType(stateMessage.getSignerHash());
        return !publisherNodeTypeToStateMessageTypesMap.containsKey(nodeType) ||
                !publisherNodeTypeToStateMessageTypesMap.get(nodeType).contains(StateMessageType.getName(stateMessage.getClass()));
    }

    protected boolean incorrectMessageSenderSignature(StateMessageData stateMessage) {
        if (!stateMessageCrypto.verifySignature(stateMessage)) {
            log.error("State message signature verification failed: {}", stateMessage);
            return true;
        }
        return false;
    }
}
