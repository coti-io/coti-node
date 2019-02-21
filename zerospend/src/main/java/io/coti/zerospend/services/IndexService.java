package io.coti.zerospend.services;

import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.data.ClusterStampPreparationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

//TODO 3/6/2019 astolia: move this to ZS ClusterStampService.
@Slf4j
@Service
public class IndexService {

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

    /**
     * Creates and sends a request for preperation for cluster stamp to other nodes.
     * @param totalConfirmedTransactions the number of totally(tcc & dspc) confirmed transaction until now.
     */
    public void createAndSendClusterStampPreparationMsg(long totalConfirmedTransactions) {
        if(validateClusterStampInitiation(totalConfirmedTransactions)) {
            log.info("Starting ClusterStamp");
            ClusterStampPreparationData clusterStampPreparationData = new ClusterStampPreparationData(totalConfirmedTransactions);
            clusterStampStateCrypto.signMessage(clusterStampPreparationData);
            clusterStampService.prepareForClusterStamp(clusterStampPreparationData);
        }
    }

    /**
     * Validates if should proceed with Cluster Stamp or not according to current cluster stamp state
     * and the number of total confirmed transactions.
     * @param totalConfirmedTransactions the number of total confirmed transactions.
     * @return true if should start claster stamp. False otherwise.
     */
    private boolean validateClusterStampInitiation(long totalConfirmedTransactions){
        return totalConfirmedTransactions > GENESIS_TRANSACTIONS && (totalConfirmedTransactions % CLUSTER_STAMP_TRANSACTION_RATIO) == 0 && clusterStampService.isClusterStampOff() ;
    }
}
