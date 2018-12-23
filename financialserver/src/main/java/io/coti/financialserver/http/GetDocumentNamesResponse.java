package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeDocumentData;
import io.coti.financialserver.http.data.GetDocumentNameResponseData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetDocumentNamesResponse extends BaseResponse {
    List<GetDocumentNameResponseData> disputeDocumentNames;

    public GetDocumentNamesResponse(List<DisputeDocumentData> disputeDocuments) {
        super();

        this.disputeDocumentNames = new ArrayList<>();
        disputeDocuments.forEach(disputeDocumentData -> this.disputeDocumentNames.add(new GetDocumentNameResponseData(disputeDocumentData)));
    }

}
