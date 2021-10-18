package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.DisputeItemVoteData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArbitratorDisputeItemResponseData extends DisputeItemResponseData {

    private DisputeItemVoteResponseData arbitratorItemVote;

    public ArbitratorDisputeItemResponseData(DisputeItemData disputeItemData, Hash arbitratorHash) {
        super(disputeItemData);
        for (DisputeItemVoteData disputeItemVoteData : disputeItemData.getDisputeItemVotesData()) {
            if (disputeItemVoteData.getArbitratorHash().equals(arbitratorHash)) {
                arbitratorItemVote = new DisputeItemVoteResponseData(disputeItemVoteData);
                break;
            }
        }

    }
}
