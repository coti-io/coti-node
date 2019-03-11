package io.coti.historynode.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.historynode.services.interfaces.IAddressTransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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