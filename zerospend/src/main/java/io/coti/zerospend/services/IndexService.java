package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.services.BaseNodeIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class IndexService extends BaseNodeIndexService {

    private final int CLUSTER_STAMP_TRANSACTION_RATIO;

    private final int GENESIS_TRANSACTIONS;

    @Autowired
    private IPropagationPublisher propagationPublisher;

    @Autowired
    private ClusterStampStateCrypto clusterStampPreparationCrypto;

    @Autowired
    IndexService(@Value("${clusterstamp.transaction.ratio}") final int ratio,
                 @Value("${clusterstamp.genesis.transactions}") final int genesisTransactions){
        CLUSTER_STAMP_TRANSACTION_RATIO = ratio;
        GENESIS_TRANSACTIONS = genesisTransactions;
    }

    public void incrementAndGetDspConfirmed(long dspConfirmed) {
        if(dspConfirmed > GENESIS_TRANSACTIONS && dspConfirmed % CLUSTER_STAMP_TRANSACTION_RATIO == 0) {

            ClusterStampPreparationData clusterStampPreparationData = new ClusterStampPreparationData(dspConfirmed);
            clusterStampPreparationCrypto.signMessage(clusterStampPreparationData);

            propagationPublisher.propagate(clusterStampPreparationData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer));
        }
    }
}
