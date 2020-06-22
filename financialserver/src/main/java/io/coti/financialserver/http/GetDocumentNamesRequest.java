package io.coti.financialserver.http;

import io.coti.basenode.http.interfaces.IRequest;
import io.coti.financialserver.http.data.GetDisputeItemDetailData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetDocumentNamesRequest implements IRequest {

    @NotNull
    private @Valid GetDisputeItemDetailData disputeDocumentNamesData;
}
