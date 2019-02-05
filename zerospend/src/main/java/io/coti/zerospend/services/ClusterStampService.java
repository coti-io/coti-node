package io.coti.zerospend.services;

import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Override
    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
        boolean bp = true;
    }
}