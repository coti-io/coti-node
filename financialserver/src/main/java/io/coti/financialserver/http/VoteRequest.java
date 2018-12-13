package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.DisputeItemVoteData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class VoteRequest extends Request {

    @NotNull
    private @Valid DisputeItemVoteData disputeItemVoteData;
}
