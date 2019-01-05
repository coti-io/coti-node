package io.coti.financialserver.http.data;

import io.coti.financialserver.data.DisputeCommentData;
import lombok.Data;

import java.time.Instant;

@Data
public class GetCommentResponseData {
    private String comment;
    private String commentSide;
    private Instant creationTime;

    public GetCommentResponseData(DisputeCommentData disputeCommentData) {
        this.comment = disputeCommentData.getComment();
        this.commentSide = disputeCommentData.getCommentSide().toString();
        this.creationTime = disputeCommentData.getCreationTime();
    }
}
