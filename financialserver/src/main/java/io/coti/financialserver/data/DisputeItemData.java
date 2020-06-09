package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class DisputeItemData implements Serializable {

    private static final long serialVersionUID = 7650553381348169678L;
    @NotNull
    private Long id;
    private BigDecimal price;
    private int quantity;
    private String name;
    @NotNull
    private DisputeReason reason;
    private DisputeItemStatus status = DisputeItemStatus.RECALL;
    private List<Hash> disputeDocumentHashes = new ArrayList<>();
    private List<Hash> disputeCommentHashes = new ArrayList<>();
    private List<DisputeItemVoteData> disputeItemVotesData = new ArrayList<>();
    private Instant arbitratorsDecisionTime;

    public void addDocumentHash(Hash documentHash) {
        disputeDocumentHashes.add(documentHash);
    }

    public void addCommentHash(Hash commentHash) {
        disputeCommentHashes.add(commentHash);
    }

    public void addItemVoteData(DisputeItemVoteData disputeItemVoteData) {
        disputeItemVotesData.add(disputeItemVoteData);
    }

    public boolean arbitratorAlreadyVoted(Hash arbitratorHash) {
        for (DisputeItemVoteData disputeItemVoteData : disputeItemVotesData) {
            if (disputeItemVoteData.getArbitratorHash().equals(arbitratorHash)) {
                return true;
            }
        }
        return false;
    }
}
