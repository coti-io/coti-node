package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class ClusterStampData implements IPropagatable, ISignable, ISignValidatable {

    private Hash hash;
    private long totalConfirmedTransactionsPriorClusterStamp;
    private Map<Hash, BigDecimal> balanceMap;
    private List<InitialFundData> initialFundDataList; // Exist only in initial Cluster Stamp
    private Map<Hash, TransactionData> unconfirmedTransactions;
    private Hash zeroSpendHash;
    private SignatureData zeroSpendSignature;

    public ClusterStampData() {
        hash = new Hash(0);
        balanceMap = new ConcurrentHashMap<>();
        unconfirmedTransactions = new ConcurrentHashMap<>();
        initialFundDataList = new ArrayList<>();
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return zeroSpendSignature;
    }

    @Override
    public Hash getSignerHash() {
        return zeroSpendHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        zeroSpendHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        zeroSpendSignature = signature;
    }
}