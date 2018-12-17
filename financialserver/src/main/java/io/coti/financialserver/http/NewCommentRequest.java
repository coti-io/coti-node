package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.DisputeCommentData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class NewCommentRequest extends Request {

    @NotNull
    private @Valid DisputeCommentData disputeCommentData;
}
