package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.DisputeItemVoteStatus;

import java.util.Date;

public class ArbitratorDisputeItemResponseData extends DisputeItemResponseData {
    private DisputeItemVoteStatus voteStatus;
    private Date voteTime;

    public ArbitratorDisputeItemResponseData(DisputeItemData disputeItemData, Hash arbitratorHash) {
        super(disputeItemData);

    }
}
