package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetDocumentRequest extends Request {
    @NotNull
    private Hash userHash;

    @NotNull
    private Integer disputeId;

    @NotNull
    private Integer documentId;

    @NotNull
    private SignatureData signature;


    public Hash getUserHash() {
        return userHash;
    }


    public Integer getDisputeId() {
        return disputeId;
    }


    public Integer getDocumentId() {
        return documentId;
    }

    public SignatureData getSignature() {
        return signature;
    }
}
