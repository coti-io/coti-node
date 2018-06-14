package io.coti.cotinode.service;

import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Service;

@Service
public class ClusterService {
    public TransactionData addToCluster(TransactionData transactionData) {
        return transactionData;
    }
}
