package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.VoteMessageCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.LockData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.VoteResult;
import io.coti.basenode.data.messages.*;
import io.coti.basenode.model.VoteResults;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.IVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class BaseNodeVoteService implements IVoteService {

    private static final int CONSENSUS_A = 1;
    private static final int CONSENSUS_B = 2;
    private static final int CONSENSUS_C = 0;
    private static final long REQUIRED_NUMBER_OF_GOOD_HISTORY_NODES = 1;
    @Autowired
    protected IPropagationPublisher propagationPublisher;
    @Autowired
    private INetworkService networkService;
    @Autowired
    protected VoteMessageCrypto voteMessageCrypto;
    @Autowired
    protected VoteResults voteResults;
    @Autowired
    protected IClusterStampService clusterStampService;
    protected final LockData voteResultLockData = new LockData();
    private final EnumMap<NodeType, List<VoteMessageType>> publisherNodeTypeToVoteTypesMap = new EnumMap<>(NodeType.class);
    protected boolean clusterStampHashVoteDone;

    public void init() {
        publisherNodeTypeToVoteTypesMap.put(NodeType.DspNode,
                Arrays.asList(VoteMessageType.CLUSTER_STAMP_INDEX_VOTE,
                        VoteMessageType.CLUSTER_STAMP_HASH_VOTE));
        publisherNodeTypeToVoteTypesMap.put(NodeType.HistoryNode,
                Collections.singletonList(VoteMessageType.CLUSTER_STAMP_AGREED_HASH_HISTORY_NODE));
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void handleVoteMessage(VoteMessageData voteMessageData) {
        log.info("General type vote received: " + VoteMessageType.getName(voteMessageData.getClass()));
        if (invalidMessageSender(voteMessageData)) {
            return;
        }
        if (invalidMessageSenderSignature(voteMessageData)) {
            return;
        }
        boolean consensusReached = false;
        boolean consensusPositive = false;
        Hash voteResultsHash = voteCombinedHash(Objects.requireNonNull(VoteMessageType.getName(voteMessageData.getClass())), voteMessageData.getVoteHash());
        try {
            synchronized (voteResultLockData.addLockToLockMap(voteResultsHash)) {
                VoteResult voteResult = voteResults.getByHash(voteResultsHash);
                if (voteResult == null) {
                    voteResult = new VoteResult(voteResultsHash, null);
                }
                voteResult.getHashToVoteMapping().put(voteMessageData.getSignerHash(), voteMessageData);
                if (!voteResult.isConsensusReached()) {
                    consensusReached = checkConsensusAndSetResult(voteResult, VoteMessageType.getName(voteMessageData.getClass()));
                }
                if (consensusReached) {
                    consensusPositive = voteResult.isConsensusPositive();
                }
                voteResults.put(voteResult);
            }
        } finally {
            voteResultLockData.removeLockFromLocksMap(voteResultsHash);
        }

        continueHandleGeneralVoteMessage(consensusReached, consensusPositive, voteMessageData);
    }

    private Hash voteCombinedHash(VoteMessageType messageType, Hash voteHash) {
        byte[] voteHashBytes = voteHash.getBytes();
        return new Hash(ByteBuffer.allocate(Integer.BYTES + voteHashBytes.length).putInt(messageType.ordinal()).put(voteHashBytes).array());
    }

    @Override
    public List<VoteMessageData> getVoteResultVotersList(VoteMessageType voteType, Hash voteHash) {
        Hash generalVoteResultsHash = voteCombinedHash(voteType, voteHash);
        VoteResult voteResult = voteResults.getByHash(generalVoteResultsHash);
        return new ArrayList<>(voteResult.getHashToVoteMapping().values());
    }

    protected void continueHandleGeneralVoteMessage(boolean consensusReached, boolean consensusPositive, VoteMessageData generalVoteMessage) {
        if (VoteMessageType.CLUSTER_STAMP_AGREED_HASH_HISTORY_NODE.equals(VoteMessageType.getName(generalVoteMessage.getClass())) &&
                consensusReached && consensusPositive) {
            clusterStampService.setAgreedHistoryNodesNumberEnough();
        }
    }

    @Override
    public long calculateQuorumOfValidators(VoteMessageType messageType) {
        if (VoteMessageType.CLUSTER_STAMP_AGREED_HASH_HISTORY_NODE.equals(messageType)) {
            return REQUIRED_NUMBER_OF_GOOD_HISTORY_NODES;
        } else {
            long numberOfValidators = networkService.countDSPNodes();
            return numberOfValidators * CONSENSUS_A / CONSENSUS_B + CONSENSUS_C;
        }
    }

    private boolean checkConsensusAndSetResult(VoteResult voteResult, VoteMessageType messageType) {
        long quorumOfValidators = calculateQuorumOfValidators(messageType);
        if (quorumOfValidators <= voteResult.getHashToVoteMapping().values().stream().filter(VoteMessageData::isVote).count()) {
            voteResult.setConsensusReached(true);
            voteResult.setConsensusPositive(true);
            return true;
        } else if (quorumOfValidators <= voteResult.getHashToVoteMapping().values().stream().filter(v -> !v.isVote()).count()) {
            voteResult.setConsensusReached(true);
            voteResult.setConsensusPositive(false);
            return true;
        }
        return false;
    }

    @Override
    public void startCollectingVotes(StateMessageData stateMessage, VoteMessageData myVote) {
        Hash generalVoteResultsHash;
        if (myVote != null) {
            generalVoteResultsHash = voteCombinedHash(Objects.requireNonNull(VoteMessageType.getName(myVote.getClass())), myVote.getVoteHash());
        } else {
            generalVoteResultsHash = stateMessage.getHash();
        }
        try {
            synchronized (voteResultLockData.addLockToLockMap(generalVoteResultsHash)) {
                VoteResult voteResult = voteResults.getByHash(generalVoteResultsHash);
                if (voteResult == null) {
                    voteResult = new VoteResult(generalVoteResultsHash, stateMessage);
                } else if (voteResult.getTheMatterOfVoting() == null) {
                    voteResult.setTheMatterOfVoting(stateMessage);
                }
                if (myVote != null) {
                    voteResult.getHashToVoteMapping().put(myVote.getSignerHash(), myVote);
                }
                voteResults.put(voteResult);
            }
        } finally {
            voteResultLockData.removeLockFromLocksMap(generalVoteResultsHash);
        }
    }

    protected VoteMessageData castVote(VoteMessageData voteMessageData, String logMessage) {
        voteMessageData.setHash(new Hash(voteMessageCrypto.getSignatureMessage(voteMessageData)));
        voteMessageCrypto.signMessage(voteMessageData);
        propagationPublisher.propagate(voteMessageData, Arrays.asList(NodeType.DspNode, NodeType.ZeroSpendServer, NodeType.NodeManager));
        log.info("Vote for " + logMessage + " " + (voteMessageData.isVote() ? "True " : "False ") + voteMessageData.getHash().toString());
        return voteMessageData;
    }

    @Override
    public VoteMessageData castVoteForClusterStampIndex(Hash voteHash, boolean vote) {
        LastIndexClusterStampVoteMessageData lastIndexClusterStampVoteMessageData = new LastIndexClusterStampVoteMessageData(voteHash, vote, Instant.now());
        castVote(lastIndexClusterStampVoteMessageData, "clusterstamp highest index");
        return lastIndexClusterStampVoteMessageData;
    }

    @Override
    public VoteMessageData castVoteForClusterStampHash(Hash voteHash, boolean vote, Hash clusterStampHash) {
        HashClusterStampVoteMessageData hashClusterStampVoteMessageData = new HashClusterStampVoteMessageData(clusterStampHash, voteHash, vote, Instant.now());
        castVote(hashClusterStampVoteMessageData, "clusterstamp hash");
        return hashClusterStampVoteMessageData;
    }

    protected boolean invalidMessageSender(VoteMessageData voteMessageData) {
        NodeType nodeType = networkService.getNetworkNodeType(voteMessageData.getSignerHash());
        return !publisherNodeTypeToVoteTypesMap.containsKey(nodeType) ||
                !publisherNodeTypeToVoteTypesMap.get(nodeType).contains(VoteMessageType.getName(voteMessageData.getClass()));
    }

    protected boolean invalidMessageSenderSignature(VoteMessageData generalVoteMessage) {
        if (!voteMessageCrypto.verifySignature(generalVoteMessage)) {
            log.error("State message signature verification failed: {}", generalVoteMessage);
            return true;
        }
        return false;
    }

    @Override
    public void clearClusterStampHashVoteDone() {
        clusterStampHashVoteDone = false;
    }

}
