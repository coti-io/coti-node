package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeDocumentData;
import io.coti.financialserver.http.data.DocumentNameResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewDocumentResponse extends BaseResponse {

    private DocumentNameResponseData document;

    public NewDocumentResponse(DisputeDocumentData disputeDocumentData) {
        this.document = new DocumentNameResponseData(disputeDocumentData);
    }
}
