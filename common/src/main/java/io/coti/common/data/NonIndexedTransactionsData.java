package io.coti.common.data;

import io.coti.common.communication.DspVote;
import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.util.Map;

@Data
public class NonIndexedTransactionsData implements IEntity {
    private Hash hash;
    private Map<Hash, DspVote> dspVoteMap;

    private NonIndexedTransactionsData() {
    }

    public NonIndexedTransactionsData(Hash hash, Map<Hash, DspVote> dspVoteMap) {
        this.hash = hash;
        this.dspVoteMap = dspVoteMap;
    }
}
