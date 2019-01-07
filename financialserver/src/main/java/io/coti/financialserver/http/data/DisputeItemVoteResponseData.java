package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.financialserver.data.DisputeItemVoteData;
import io.coti.financialserver.data.DisputeItemVoteStatus;

import java.time.Instant;

public class DisputeItemVoteResponseData {
    private String arbitratorHash;
    private String disputeHash;
    private Long itemId;
    private DisputeItemVoteStatus status;
    private Instant voteTime;
    private SignatureData arbitratorSignature;

    public DisputeItemVoteResponseData(DisputeItemVoteData disputeItemVoteData) {
        this.arbitratorHash = disputeItemVoteData.getArbitratorHash().toString();
        this.disputeHash = disputeItemVoteData.getDisputeHash().toString();
        this.itemId = disputeItemVoteData.getItemId();
        this.status = disputeItemVoteData.getStatus();
        this.voteTime = disputeItemVoteData.getVoteTime();
        this.arbitratorSignature = disputeItemVoteData.getArbitratorSignature();

    }

}
