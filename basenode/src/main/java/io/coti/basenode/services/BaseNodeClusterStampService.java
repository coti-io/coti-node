package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.ClusterStamps;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * An abstract class that provides basic Cluster Stamp functionality for all nodes that take part in cluster stamp flow.
 */
@Slf4j
@Service
public class BaseNodeClusterStampService implements IClusterStampService {

    protected static final Hash LAST_CLUSTER_STAMP_HASH = new Hash(0);

    @Value("${recovery.server.address:#{null}}")
    private String recoveryServerAddress;
    @Autowired
    protected IPropagationPublisher propagationPublisher;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    protected TccConfirmationService tccConfirmationService;
    @Autowired
    protected Transactions transactions;
    @Autowired
    protected ClusterStamps clusterStamps;
    @Autowired
    protected ClusterStampCrypto clusterStampCrypto;

    protected Map<Hash, TransactionData> getUnconfirmedTransactions() {
        return new HashMap<>();
    }

    @Override
    public void loadBalanceFromLastClusterStamp() {

        ClusterStampData clusterStampData = getLastClusterStamp();

        if(clusterStampData != null) {
            loadBalanceFromClusterStamp(clusterStampData);
        }
    }

    protected void loadBalanceFromClusterStamp(ClusterStampData clusterStampData) {

        balanceService.updateBalanceAndPreBalanceMap(clusterStampData.getBalanceMap());
        transactions.deleteAll();
        Iterator it = clusterStampData.getUnconfirmedTransactions().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry unConfirmedTransaction = (Map.Entry)it.next();
            transactions.put( (TransactionData)unConfirmedTransaction.getValue() );
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public ClusterStampData getNewerClusterStamp(long totalConfirmedTransactionsPriorClusterStamp) {
        ClusterStampData lastClusterStampData = getLastClusterStamp();
        if(lastClusterStampData.getTotalConfirmedTransactionsPriorClusterStamp() > totalConfirmedTransactionsPriorClusterStamp) {
            return lastClusterStampData;
        }
        return null;
    }

    protected ClusterStampData getLastClusterStamp() {

        long totalConfirmedTransactionsPriorClusterStamp;

        ClusterStampData localClusterStampData = clusterStamps.getByHash(LAST_CLUSTER_STAMP_HASH);
        if (localClusterStampData != null) {
            totalConfirmedTransactionsPriorClusterStamp = localClusterStampData.getTotalConfirmedTransactionsPriorClusterStamp();
        }
        else {
            totalConfirmedTransactionsPriorClusterStamp = -1;
        }

        if(!recoveryServerAddress.isEmpty()) {
            RestTemplate restTemplate = new RestTemplate();
            ClusterStampData lastClusterStampData =
                    restTemplate.postForObject(
                            recoveryServerAddress + "/getLastClusterStamp",
                            totalConfirmedTransactionsPriorClusterStamp,
                            ClusterStampData.class);

            if(lastClusterStampData != null && clusterStampCrypto.verifySignature(lastClusterStampData)) {
                clusterStamps.put(lastClusterStampData);
                log.info("Received last cluster stamp for total confirmed transactions: {}", totalConfirmedTransactionsPriorClusterStamp);
                return lastClusterStampData;
            }
        }

        return localClusterStampData;
    }
}