package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DisputeItemData implements Serializable, ISignable, ISignValidatable {

    private Hash userHash;
    @NotNull
    private Long id;
    private BigDecimal price;
    @NotNull
    private int quantity;
    @NotNull
    private DisputeReason reason;
    private Hash disputeHash;
    private DisputeItemStatus status;
    private List<Hash> disputeDocumentHashes;
    private List<Hash> disputeCommentHashes;
    private List<DisputeItemVoteData> disputeItemVotesData;
    private SignatureData userSignature;

    public DisputeItemData() {
        disputeDocumentHashes = new ArrayList<>();
        disputeCommentHashes = new ArrayList<>();
        disputeItemVotesData = new ArrayList<>();
        status = DisputeItemStatus.Recall;
    }

    public void addDocumentHash(Hash documentHash) {
        disputeDocumentHashes.add(documentHash);
    }

    public void addCommentHash(Hash commentHash) {
        disputeCommentHashes.add(commentHash);
    }

    public void addItemVoteData(DisputeItemVoteData disputeItemVoteData) {
        disputeItemVotesData.add(disputeItemVoteData);
    }

    public Boolean arbitratorAlreadyVoted(Hash arbitratorHash) {
        for (DisputeItemVoteData disputeItemVoteData : disputeItemVotesData) {
            if (disputeItemVoteData.getUserHash().equals(arbitratorHash)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        userHash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.userSignature = signature;
    }
}
