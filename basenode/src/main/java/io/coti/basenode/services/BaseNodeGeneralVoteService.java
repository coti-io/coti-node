package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.GeneralMessageCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.messages.GeneralMessageType;
import io.coti.basenode.data.messages.GeneralVoteClusterStampIndexPayload;
import io.coti.basenode.data.messages.GeneralVoteMessage;
import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.services.interfaces.IGeneralVoteService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

@Slf4j
@Service
public class BaseNodeGeneralVoteService implements IGeneralVoteService {

    @Autowired
    protected IPropagationPublisher propagationPublisher;
    @Autowired
    private INetworkService networkService;
    @Autowired
    protected GeneralMessageCrypto generalMessageCrypto;

    private EnumMap<NodeType, List<GeneralMessageType>> publisherNodeTypeToGeneralVoteTypesMap = new EnumMap<>(NodeType.class);

    public void init() {
        publisherNodeTypeToGeneralVoteTypesMap.put(NodeType.DspNode,
                Arrays.asList(GeneralMessageType.CLUSTER_STAMP_INDEX_VOTE,
                        GeneralMessageType.CLUSTER_STAMP_HASH_VOTE));
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void handleGeneralVoting(GeneralVoteMessage generalVoteMessage) {
        log.info("General type vote received: " + generalVoteMessage.getMessagePayload().getGeneralMessageType().toString());
        continueHandleStateMessage(generalVoteMessage);
    }

    protected void continueHandleStateMessage(GeneralVoteMessage generalVoteMessage) {
        // implemented in subclasses
    }

    @Override
    public void castVoteForClusterstampIndex(Hash voteHash, boolean vote) {
        GeneralVoteClusterStampIndexPayload generalVoteClusterStampIndexPayload = new GeneralVoteClusterStampIndexPayload(voteHash, vote);
        GeneralVoteMessage generalVoteMessage = new GeneralVoteMessage(generalVoteClusterStampIndexPayload);
        generalVoteMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(generalVoteMessage)));
        generalMessageCrypto.signMessage(generalVoteMessage);
        propagationPublisher.propagate(generalVoteMessage, Arrays.asList(NodeType.DspNode, NodeType.ZeroSpendServer, NodeType.NodeManager));
        log.info("Vote for clusterstamp " + (vote ? "True " : "False ") + generalVoteMessage.getHash().toString());
    }

    protected boolean incorrectMessageSender(StateMessage stateMessage) {
        NodeType nodeType = networkService.getNetworkNodeType(stateMessage.getSignerHash());
        return !publisherNodeTypeToGeneralVoteTypesMap.containsKey(nodeType) ||
                !publisherNodeTypeToGeneralVoteTypesMap.get(nodeType).contains(stateMessage.getMessagePayload().getGeneralMessageType());
    }

    protected boolean incorrectMessageSenderSignature(StateMessage stateMessage) {
        if (!generalMessageCrypto.verifySignature(stateMessage)) {
            log.error("State message signature verification failed: {}", stateMessage);
            return true;
        }
        return false;
    }
}
