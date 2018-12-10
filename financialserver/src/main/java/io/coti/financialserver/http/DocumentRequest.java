package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.DisputeDocumentData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class DocumentRequest extends Request {

    @NotNull
    private @Valid DisputeDocumentData disputeDocumentData;
}
