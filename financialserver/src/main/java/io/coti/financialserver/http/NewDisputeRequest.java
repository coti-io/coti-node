package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.DisputeData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class NewDisputeRequest extends Request {

    @NotNull
    private @Valid DisputeData disputeData;
}
