package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeDocumentData;
import io.coti.financialserver.http.data.DocumentNameResponseData;
import lombok.Data;

@Data
public class NewDocumentResponse extends BaseResponse {

    private DocumentNameResponseData document;

    public NewDocumentResponse(DisputeDocumentData disputeDocumentData) {
        super();
        this.document = new DocumentNameResponseData(disputeDocumentData);
    }
}
