package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DisputeEventReadRequest extends Request {

    @NotNull
    private Hash userHash;
    @NotNull
    private Hash disputeEventHash;
}
