package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.GeneralMessageCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.messages.*;
import io.coti.basenode.model.GeneralVoteResults;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.IGeneralVoteService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    @Autowired
    protected GeneralVoteResults generalVoteResults;
    @Autowired
    protected IClusterStampService clusterStampService;

    protected final LockData generalVoteResultLockData = new LockData();
    private static final int CONSENSUS_A = 1;
    private static final int CONSENSUS_B = 2;
    private static final int CONSENSUS_C = 0;
    private static final long REQUIRED_NUMBER_OF_GOOD_HISTORY_NODES = 1;

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
        if (incorrectMessageSender(generalVoteMessage)) {
            return;
        }
        if (incorrectMessageSenderSignature(generalVoteMessage)) {
            return;
        }
        boolean consensusReached = false;
        boolean consensusPositive = false;
        GeneralVote newVote = new GeneralVote(generalVoteMessage);
        try {
            synchronized (generalVoteResultLockData.addLockToLockMap(generalVoteMessage.getVoteHash())) {
                GeneralVoteResult generalVoteResult = generalVoteResults.getByHash(generalVoteMessage.getVoteHash());
                if (generalVoteResult == null) {
                    generalVoteResult = new GeneralVoteResult(generalVoteMessage.getVoteHash(), null);
                }
                generalVoteResult.getHashToVoteMapping().put(newVote.getVoterHash(), newVote);
                if (!generalVoteResult.isConsensusReached()) {
                    consensusReached = checkConsensusAndSetResult(generalVoteResult, generalVoteMessage.getMessagePayload().getGeneralMessageType());
                }
                if (consensusReached) {
                    consensusPositive = generalVoteResult.isConsensusPositive();
                }
                generalVoteResults.put(generalVoteResult);
            }
        } finally {
            generalVoteResultLockData.removeLockFromLocksMap(generalVoteMessage.getVoteHash());
        }

        continueHandleGeneralVoteMessage(consensusReached, consensusPositive, generalVoteMessage);
    }

    @Override
    public List<GeneralVoteMessage> getVoteResultVotersList(Hash voteHash) {
        GeneralVoteResult generalVoteResult = generalVoteResults.getByHash(voteHash);
        List<GeneralVoteMessage> voteResultVotersList = new ArrayList<>();
        GeneralVoteClusterStampHashPayload generalVoteClusterStampHashPayload = new GeneralVoteClusterStampHashPayload();
        generalVoteResult.getHashToVoteMapping().values().forEach(v -> {
            GeneralVoteMessage generalVoteMessage = new GeneralVoteMessage(voteHash, v, generalVoteClusterStampHashPayload);
            generalVoteMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(generalVoteMessage)));
            voteResultVotersList.add(generalVoteMessage);
        });
        return voteResultVotersList;
    }

    protected void continueHandleGeneralVoteMessage(boolean consensusReached, boolean consensusPositive, GeneralVoteMessage generalVoteMessage) {
        if (GeneralMessageType.CLUSTER_STAMP_HASH_HISTORY_NODE.equals(generalVoteMessage.getMessagePayload().getGeneralMessageType())) {
            if (consensusReached && consensusPositive) {
                clusterStampService.setAgreedHistoryNodesNumberEnough();
            }
        }
    }

    @Override
    public long calculateQuorumOfValidators(GeneralMessageType messageType) {
        if (GeneralMessageType.CLUSTER_STAMP_HASH_HISTORY_NODE.equals(messageType)){
            return REQUIRED_NUMBER_OF_GOOD_HISTORY_NODES;
        } else {
            long numberOfValidators = networkService.countDSPNodes();
            return numberOfValidators * CONSENSUS_A / CONSENSUS_B + CONSENSUS_C;
        }
    }

    private boolean checkConsensusAndSetResult(GeneralVoteResult generalVoteResult, GeneralMessageType messageType) {
        long quorumOfValidators = calculateQuorumOfValidators(messageType);
        if (quorumOfValidators <= generalVoteResult.getHashToVoteMapping().values().stream().filter(v -> v.isVote()).count()) {
            generalVoteResult.setConsensusReached(true);
            generalVoteResult.setConsensusPositive(true);
            return true;
        } else if (quorumOfValidators <= generalVoteResult.getHashToVoteMapping().values().stream().filter(v -> !v.isVote()).count()) {
            generalVoteResult.setConsensusReached(true);
            generalVoteResult.setConsensusPositive(false);
            return true;
        }
        return false;
    }

    @Override
    public void startCollectingVotes(StateMessage stateMessage, GeneralVoteMessage myVote) {
        GeneralVote newVote = new GeneralVote(myVote);
        try {
            synchronized (generalVoteResultLockData.addLockToLockMap(stateMessage.getHash())) {
                GeneralVoteResult generalVoteResult = generalVoteResults.getByHash(stateMessage.getHash());
                if (generalVoteResult == null) {
                    generalVoteResult = new GeneralVoteResult(stateMessage.getHash(), stateMessage.getMessagePayload());
                } else if (generalVoteResult.getTheMatter() == null) {
                    generalVoteResult.setTheMatter(stateMessage.getMessagePayload());
                }
                generalVoteResult.getHashToVoteMapping().put(newVote.getVoterHash(), newVote);
                generalVoteResults.put(generalVoteResult);
            }
        } finally {
            generalVoteResultLockData.removeLockFromLocksMap(stateMessage.getHash());
        }
    }

    protected GeneralVoteMessage castVote(MessagePayload messagePayload, Hash voteHash, boolean vote, String logMessage) {
        GeneralVoteMessage generalVoteMessage = new GeneralVoteMessage(messagePayload, voteHash, vote);
        generalVoteMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(generalVoteMessage)));
        generalMessageCrypto.signMessage(generalVoteMessage);
        propagationPublisher.propagate(generalVoteMessage, Arrays.asList(NodeType.DspNode, NodeType.ZeroSpendServer, NodeType.NodeManager));
        log.info("Vote for " + logMessage + " " + (vote ? "True " : "False ") + generalVoteMessage.getHash().toString());
        return generalVoteMessage;
    }

    @Override
    public GeneralVoteMessage castVoteForClusterStampIndex(Hash voteHash, boolean vote) {
        GeneralVoteClusterStampIndexPayload generalVoteClusterStampIndexPayload = new GeneralVoteClusterStampIndexPayload();
        return castVote(generalVoteClusterStampIndexPayload, voteHash, vote, "clusterstamp highest index");
    }

    @Override
    public GeneralVoteMessage castVoteForClusterStampHash(Hash voteHash, boolean vote) {
        GeneralVoteClusterStampHashPayload generalVoteClusterStampHashPayload = new GeneralVoteClusterStampHashPayload();
        return castVote(generalVoteClusterStampHashPayload, voteHash, vote, "clusterstamp hash");
    }

    protected boolean incorrectMessageSender(GeneralVoteMessage generalVoteMessage) {
        NodeType nodeType = networkService.getNetworkNodeType(generalVoteMessage.getSignerHash());
        return !publisherNodeTypeToGeneralVoteTypesMap.containsKey(nodeType) ||
                !publisherNodeTypeToGeneralVoteTypesMap.get(nodeType).contains(generalVoteMessage.getMessagePayload().getGeneralMessageType());
    }

    protected boolean incorrectMessageSenderSignature(GeneralVoteMessage generalVoteMessage) {
        if (!generalMessageCrypto.verifySignature(generalVoteMessage)) {
            log.error("State message signature verification failed: {}", generalVoteMessage);
            return true;
        }
        return false;
    }
}
