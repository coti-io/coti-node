package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.ClusterStampPreparationCrypto;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.services.BaseNodeIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class IndexService extends BaseNodeIndexService {

    private static final int MAKE_CLUSTER_STAMP_EACH_TRANSACTION = 1;

    @Autowired
    private IPropagationPublisher propagationPublisher;

    @Autowired
    private ClusterStampPreparationCrypto clusterStampPreparationCrypto;

    public void incrementAndGetDspConfirmed(long dspConfirmed) {

        if(dspConfirmed > 11 && dspConfirmed % MAKE_CLUSTER_STAMP_EACH_TRANSACTION == 0) {

            ClusterStampPreparationData clusterStampPreparationData = new ClusterStampPreparationData(dspConfirmed);
            clusterStampPreparationCrypto.signMessage(clusterStampPreparationData);

            propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer));
        }
    }
}
