package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class DspConsensusResult extends ConfirmationData implements Serializable, IEntity {
    private Hash transactionHash;
    private Hash zeroSpendServerHash;
    private long index;
    private Date indexingTime;
    private SignatureData signatureData;
    private List<DspVote> votesList;
    private boolean isDspConsensus;

    public DspConsensusResult(Hash transactionHash) {
        this.transactionHash = transactionHash;
    }

    private DspConsensusResult() {
    }

    @Override
    public Hash getHash() {
        return transactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionHash = hash;
    }
}