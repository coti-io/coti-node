package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
public class GetAddressesRequest extends Request implements ISignable, ISignValidatable  {
    @NotNull(message = "Address hash must not be null")
    private Set<Hash> addressesHash;
    @NotNull
    private Hash signerHash;
    @NotNull
    private SignatureData signature;

    public GetAddressesRequest(Set<Hash> addressesHash) {
        this.addressesHash = addressesHash;
    }

    public GetAddressesRequest() {
        this.addressesHash = new HashSet<>();
    }

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return signerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.signerHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }
}