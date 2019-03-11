package io.coti.storagenode.services;

import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.data.ClusterStampStateData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Handler for PrepareForSnapshot messages propagated to HistoryNode.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Override
    public Set<Hash> getUnreachedDspcHashTransactions() {
        return null;
    }

    @Override
    public void handleClusterStampData(ClusterStampData clusterStampData) {

    }

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {

    }

    @Override
    public void getReadyForClusterStamp(ClusterStampStateData nodeReadyForClusterStampData) {

    }
}