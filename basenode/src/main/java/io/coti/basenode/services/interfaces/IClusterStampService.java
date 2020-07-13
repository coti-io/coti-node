package io.coti.basenode.services.interfaces;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.*;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

public interface IClusterStampService {

    long CLUSTER_STAMP_TIMEOUT = 100;

    void init();

    boolean shouldUpdateClusterStampDBVersion();

    boolean isClusterStampDBVersionExist();

    void setClusterStampDBVersion();

    ResponseEntity<IResponse> getRequiredClusterStampNames();

    void getClusterStampFromRecoveryServer();

    default void clusterStampInitiate(StateMessageData stateMessage, InitiateClusterStampStateMessageData initiateClusterStampStateMessageData) {
    }

    void clusterStampContinueWithIndex(LastIndexClusterStampStateMessageData lastIndexClusterStampStateMessageData);

    default void clusterStampContinueWithHash(StateMessageData stateMessage) {
    }

    void clusterStampExecute(ExecuteClusterStampStateMessageData executeClusterStampStateMessageData);

    Hash getCandidateClusterStampHash();

    void updateVoteMessageClusterStampSegment(boolean prepareClusterStampLines, VoteMessageData generalVoteMessage);

    default void calculateClusterStampDataAndHashesAndSendMessage() {
    }

    default void doClusterStampAfterVoting(Hash voteHash) {
    }

    void calculateClusterStampDataAndHashes();

    void calculateClusterStampDataAndHashes(Instant clusterStampInitiateTime);

    boolean checkLastConfirmedIndex(LastIndexClusterStampStateMessageData stateMessageLastClusterStampIndexPayload);

    boolean checkClusterStampHash(HashClusterStampStateMessageData hashClusterStampStateMessageData);

    void setAgreedHistoryNodesNumberEnough();

    boolean isAgreedHistoryNodesNumberEnough();
}