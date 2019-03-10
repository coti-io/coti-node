package io.coti.financialserver.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.crypto.DspClusterStampVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.DspReadyForClusterStamp;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A service that provides Cluster Stamp functionality for DSP node.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Override
    public Set<Hash> getUnreachedDspcHashTransactions() {
        return null;
    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {

    }

    @Override
    public void getReadyForClusterStamp(ClusterStampStateData nodeReadyForClusterStampData) {

    }
}