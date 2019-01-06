package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.financialserver.http.data.GetDocumentFileData;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetDocumentFileRequest {
    @NotNull
    private Hash userHash;
    @NotNull
    private String r;
    @NotNull
    private String s;
    @NotNull
    private Hash documentHash;

    public GetDocumentFileData getDocumentFileData() {
        return new GetDocumentFileData(documentHash, userHash, new SignatureData(r, s));
    }
}
