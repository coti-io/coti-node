package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.http.data.GetDisputeItemDetailData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetDocumentNamesRequest extends Request {
    @NotNull
    private @Valid GetDisputeItemDetailData disputeDocumentNamesData;
}
