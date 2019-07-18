package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetHistoryAddressesRequest extends Request implements ISignable, ISignValidatable {

    @NotEmpty(message = "Address hash must not be null")
    private List<Hash> addressesHash;
    private Hash signerHash;
    private SignatureData signature;

    public GetHistoryAddressesRequest() {
        this.addressesHash = new ArrayList<>();
    }

    public GetHistoryAddressesRequest(List<Hash> addressesHash) {
        this.addressesHash = addressesHash;
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
