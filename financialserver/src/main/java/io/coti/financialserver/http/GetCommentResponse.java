package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeCommentData;
import lombok.Data;

@Data
public class GetCommentResponse extends BaseResponse {

    private DisputeCommentData disputeCommentData;

    public GetCommentResponse(DisputeCommentData disputeCommentData) {
        super();
        this.disputeCommentData = disputeCommentData;
    }
}
