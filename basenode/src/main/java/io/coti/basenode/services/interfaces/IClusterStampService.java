package io.coti.basenode.services.interfaces;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.*;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

public interface IClusterStampService {

    void init();

    boolean shouldUpdateClusterStampDBVersion();

    boolean isClusterStampDBVersionExist();

    void setClusterStampDBVersion();

    ResponseEntity<IResponse> getRequiredClusterStampNames();

    void getClusterStampFromRecoveryServer();

    void clusterStampInitiate(StateMessage stateMessage, StateMessageClusterStampInitiatedPayload stateMessageClusterstampInitiatedPayload);

    boolean checkLastConfirmedIndex(StateMessageLastClusterStampIndexPayload stateMessageLastClusterStampIndexPayload);

    void clusterStampExecute(StateMessage stateMessage, StateMessageClusterStampExecutePayload stateMessageClusterStampExecutePayload);

    Hash getCandidateClusterStampHash();

    void prepareCandidateClusterStampHash();

    void updateGeneralVoteMessageClusterStampSegment(boolean prepareClusterStampLines, GeneralVoteMessage generalVoteMessage);
}