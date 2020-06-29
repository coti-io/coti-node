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

    default void clusterStampInitiate(StateMessage stateMessage, StateMessageClusterStampInitiatedPayload stateMessageClusterstampInitiatedPayload) {
    }

    default void clusterStampContinueWithIndex(StateMessage stateMessage) {
    }

    default void clusterStampContinueWithHash(StateMessage stateMessage) {
    }

    void clusterStampExecute(StateMessage stateMessage, StateMessageClusterStampExecutePayload stateMessageClusterStampExecutePayload);

    Hash getCandidateClusterStampHash();

    void updateGeneralVoteMessageClusterStampSegment(boolean prepareClusterStampLines, GeneralVoteMessage generalVoteMessage);

    default void calculateClusterStampDataAndHashesAndSendMessage() {
    }

    default void doClusterStampAfterVoting(GeneralVoteMessage generalVoteMessage) {
    }

    void calculateClusterStampDataAndHashes();

    void calculateClusterStampDataAndHashes(Instant clusterStampInitiateTime);

    boolean checkLastConfirmedIndex(StateMessageLastClusterStampIndexPayload stateMessageLastClusterStampIndexPayload);

    boolean checkClusterStampHash(StateMessageClusterStampHashPayload stateMessageClusterStampHashPayload);

    void setAgreedHistoryNodesNumberEnough();

    boolean isAgreedHistoryNodesNumberEnough();
}