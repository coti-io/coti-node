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

@Slf4j
@Data
public class ClusterStampData implements IPropagatable, ISignable, ISignValidatable {

    //TODO 3/4/2019 astolia: see if can decrease amount of data here.
    private Hash hash;
    private long totalConfirmedTransactionsPriorClusterStamp;
    private Map<Hash, BigDecimal> balanceMap;
    private Map<Hash, TransactionData> unconfirmedTransactions;
    private List<DspReadyForClusterStampData> dspReadyForClusterStampDataList;
    private ClusterStampConsensusResult clusterStampConsensusResult;
    private Hash zeroSpendHash;
    private SignatureData zeroSpendSignature;

    public ClusterStampData() {
        this.dspReadyForClusterStampDataList = new ArrayList<>();
    }

    public void setHash() {
        byte[] balanceMapBytes = balanceMap.toString().getBytes();
        byte[] unconfirmedTransactionsBytes = unconfirmedTransactions.toString().getBytes();

        int byteBufferLength = balanceMapBytes.length + unconfirmedTransactionsBytes.length;
        ByteBuffer hashBytesBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(balanceMapBytes)
                .put(unconfirmedTransactionsBytes);
        this.hash = new Hash(hashBytesBuffer.array());
    }


    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        clusterStampConsensusResult = new ClusterStampConsensusResult(hash);
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