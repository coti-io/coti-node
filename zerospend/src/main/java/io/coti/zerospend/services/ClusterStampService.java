package io.coti.zerospend.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * A service that provides Cluster Stamp functionality for Zero Spend node.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    public static final String CLUSTERSTAMP_FILE_SUFFIX = "_clusterStamp.csv";

    @Value("${logging.file.name}")
    private String clusterStampFilePrefix;


    @Override
    protected ClusterStampData getLastClusterStamp() {
        ClusterStampData localClusterStampData = clusterStamps.getByHash(LAST_CLUSTER_STAMP_HASH);
        boolean writeSignatureToFile = false;
        ClusterStampData loadedClusterStampData = null;
        if(localClusterStampData == null) {
            loadedClusterStampData = loadInitialClusterStamp();
            writeSignatureToFile = !(loadedClusterStampData!= null && loadedClusterStampData.getSignature()!= null);
            localClusterStampData = clusterStamps.getByHash(LAST_CLUSTER_STAMP_HASH);
        }

        clusterStampCrypto.signMessage(localClusterStampData);
        if(writeSignatureToFile)
        {
            updateClusterStampFileWithSignature(localClusterStampData.getSignature());
        }
        return localClusterStampData;
    }

}