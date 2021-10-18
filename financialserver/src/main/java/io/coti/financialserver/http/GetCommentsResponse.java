package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeCommentData;
import io.coti.financialserver.http.data.GetCommentResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetCommentsResponse extends BaseResponse {

    private List<GetCommentResponseData> disputeComments;

    public GetCommentsResponse(List<DisputeCommentData> disputeComments) {
        super();

        this.disputeComments = new ArrayList<>();
        disputeComments.forEach(disputeCommentData -> this.disputeComments.add(new GetCommentResponseData(disputeCommentData)));
    }
}
