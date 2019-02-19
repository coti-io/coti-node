package io.coti.zerospend.services;

import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IndexService extends BaseNodeIndexService {

    private final int CLUSTER_STAMP_TRANSACTION_RATIO;

    private final int GENESIS_TRANSACTIONS;

    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;

    @Autowired
    private ClusterStampService clusterStampService;

    @Autowired
    IndexService(@Value("${clusterstamp.transaction.ratio}") final int ratio,
                 @Value("${clusterstamp.genesis.transactions}") final int genesisTransactions) {
        CLUSTER_STAMP_TRANSACTION_RATIO = ratio;
        GENESIS_TRANSACTIONS = genesisTransactions;
    }

    @Override
    // TODO rename.
    public void incrementAndGetTotalConfirmed(Hash transactionHash, long totalConfirmedTransactions) {
        if(totalConfirmedTransactions > GENESIS_TRANSACTIONS && (totalConfirmedTransactions % CLUSTER_STAMP_TRANSACTION_RATIO) == 0) {
            ClusterStampPreparationData clusterStampPreparationData = new ClusterStampPreparationData(totalConfirmedTransactions);
            clusterStampStateCrypto.signMessage(clusterStampPreparationData);
            clusterStampService.prepareForClusterStamp(clusterStampPreparationData);
        }
    }
}
