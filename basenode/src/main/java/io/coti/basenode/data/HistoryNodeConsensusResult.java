package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class HistoryNodeConsensusResult extends ConfirmationData implements IPropagatable, ISignable, ISignValidatable
{
    private Hash requestHash; // Address or Tx hash?   // TODO Verify if there is a need to an additional field here
    private Hash historyNodeMasterHash;
    private SignatureData historyNodeMasterSignature;
    private List<HistoryNodeVote> historyNodesVotesList;
    private Map<Hash, String> hashToObjectJsonDataMap; // TODO verify if it can fit to both Address \ Tx single \ multiple, check whether to separate or not

    public HistoryNodeConsensusResult(Hash requestHash) {
        this.requestHash = requestHash;
    }

    public HistoryNodeConsensusResult() { }



    @Override
    public Hash getHash() {
        return requestHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.requestHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return historyNodeMasterSignature;
    }

    @Override
    public Hash getSignerHash() {
        return historyNodeMasterHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.historyNodeMasterHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.historyNodeMasterSignature = signature;
    }
}
