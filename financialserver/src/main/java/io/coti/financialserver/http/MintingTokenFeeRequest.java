package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import io.coti.financialserver.data.MintingFeeWarrantData;
import io.coti.financialserver.http.data.MintingFeeData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class MintingTokenFeeRequest extends Request implements ISignValidatable {

    @NotNull
    private @Valid MintingFeeData mintingFeeData;
    @NotNull
    private @Valid Hash receiverAddress;
    @NotNull
    private @Valid Hash userHash;
    private @Valid MintingFeeWarrantData mintingFeeWarrantData;
    @NotNull
    private @Valid SignatureData signature;

    @Override
    public Hash getSignerHash() {
        return userHash;
    }
}
