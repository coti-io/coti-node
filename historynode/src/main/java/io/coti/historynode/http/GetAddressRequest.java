package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetAddressRequest implements ISignValidatable, IRequest {
    @NotNull(message = "Address hash must not be null")
    private Hash addressesHash;
    @NotNull
    private Hash userHash;
    @NotNull
    private SignatureData userSignature;

    public GetAddressRequest(Hash addressesHash) {
        this.addressesHash = addressesHash;
    }

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }
}