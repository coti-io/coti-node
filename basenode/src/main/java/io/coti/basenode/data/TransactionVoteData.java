package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class TransactionVoteData implements IEntity {

    private static final long serialVersionUID = -7551521383352561518L;
    private Hash transactionHash;
    private Map<Hash, DspVote> dspHashToVoteMapping;
    List<Hash> legalVoterDspHashes;

    private TransactionVoteData() {
    }

    public TransactionVoteData(Hash transactionHash, List<Hash> legalVoterDspHashes) {
        this.transactionHash = transactionHash;
        this.dspHashToVoteMapping = new ConcurrentHashMap<>();
        this.legalVoterDspHashes = legalVoterDspHashes;
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