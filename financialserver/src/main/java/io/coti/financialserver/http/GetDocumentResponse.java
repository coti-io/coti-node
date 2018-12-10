package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeDocumentData;
import lombok.Data;

@Data
public class GetDocumentResponse extends BaseResponse {

    private DisputeDocumentData disputeDocumentData;

    public GetDocumentResponse(DisputeDocumentData disputeDocumentData) {
        super();
        this.disputeDocumentData = disputeDocumentData;
    }
}
