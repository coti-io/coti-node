package io.coti.financialserver.http;

import lombok.Data;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeDocumentData;

@Data
public class GetDocumentResponse extends BaseResponse {

    private DisputeDocumentData disputeDocumentData;

    public GetDocumentResponse(DisputeDocumentData disputeDocumentData) {
        super();
        this.disputeDocumentData = disputeDocumentData;
    }
}
