package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class GetAddressesRequest extends Request implements ISignValidatable  {
    @NotNull(message = "Address hash must not be null")
    private Set<Hash> addressesHash;
    @NotNull
    private Hash userHash;
    @NotNull
    private SignatureData userSignature;

    public GetAddressesRequest(Set<Hash> addressesHash) {
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