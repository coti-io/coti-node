package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class SetUserTypeRequest extends Request implements ISignValidatable {

    @NotNull
    private String userType;
    @NotNull
    private Hash userHash;
    @NotNull
    private Hash signerHash;
    @NotNull
    private @Valid SignatureData signature;

}
