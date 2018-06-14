package io.coti.cotinode.service;

import io.coti.cotinode.model.TransactionPackage;
import org.springframework.stereotype.Service;

@Service
public class ClusterService {
    public boolean addToCluster(TransactionPackage transactionPackage) {
        return true;
    }
}
