package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class NewDocumentResponse extends BaseResponse {

    private String documentHash;

    public NewDocumentResponse(Hash documentHash) {
        super();
        this.documentHash = documentHash.toString();
    }
}
