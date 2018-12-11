package io.coti.financialserver.http;

import lombok.Data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;

@Data
public class NewDocumentResponse extends BaseResponse {

    private String documentHash;

    public NewDocumentResponse(Hash documentHash) {
        super();
        this.documentHash = documentHash.toString();
    }
}
