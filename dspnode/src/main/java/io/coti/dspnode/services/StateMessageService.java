package io.coti.dspnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.*;
import io.coti.basenode.services.BaseNodeStateMessageService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

import static io.coti.basenode.data.messages.StateMessageType.getName;

@Slf4j
@Service
public class StateMessageService extends BaseNodeStateMessageService {

    @Autowired
    private IClusterStampService clusterStampService;

    @Override
    public void continueHandleStateMessage(StateMessageData stateMessage) {
        switch (Objects.requireNonNull(getName(stateMessage.getClass()))) {
            case CLUSTER_STAMP_INITIATED:
                clusterStampService.clusterStampInitiate(stateMessage, (InitiateClusterStampStateMessageData) stateMessage);
                break;
            case CLUSTER_STAMP_PREPARE_INDEX:
                clusterStampService.clusterStampContinueWithIndex((LastIndexClusterStampStateMessageData) stateMessage);
                boolean vote = clusterStampService.checkLastConfirmedIndex((LastIndexClusterStampStateMessageData) stateMessage);
                voteService.startCollectingVotes(stateMessage, stateMessage.getHash(), voteService.castVoteForClusterStampIndex(stateMessage.getHash(), vote));
                if (vote) {
                    clusterStampService.calculateClusterStampDataAndHashes();  // todo separate it to a thread
                }
                break;
            case CLUSTER_STAMP_PREPARE_HASH:
                clusterStampService.clusterStampContinueWithHash(stateMessage);
                Hash candidateClusterStampHash = clusterStampService.getCandidateClusterStampHash();
                voteService.startCollectingVotes(stateMessage, candidateClusterStampHash, voteService.castVoteForClusterStampHash(clusterStampService.checkClusterStampHash((HashClusterStampStateMessageData) stateMessage),
                        candidateClusterStampHash));
                Thread waitForHistoryNodesAndCastVote = new Thread(() -> {
                    try {
                        Instant waitForHistoryNodesTill = Instant.now().plusSeconds(clusterStampService.CLUSTER_STAMP_TIMEOUT);
                        while (Instant.now().isBefore(waitForHistoryNodesTill)) {
                            if (clusterStampService.isAgreedHistoryNodesNumberEnough()) {
                                voteService.castVoteForClusterStampHash(clusterStampService.checkClusterStampHash((HashClusterStampStateMessageData) stateMessage), candidateClusterStampHash);
                                break;
                            }
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        log.info(String.format("WaitForHistoryNodesAndCastVote interrupted %s", e));
                        Thread.currentThread().interrupt();
                    }
                });
                waitForHistoryNodesAndCastVote.start();

                break;
            case CLUSTER_STAMP_EXECUTE:
                clusterStampService.clusterStampExecute((ExecuteClusterStampStateMessageData) stateMessage);
                break;
            default:
                log.error("Unexpected message type: {}", StateMessageType.getName(stateMessage.getClass()));
        }
    }

}
