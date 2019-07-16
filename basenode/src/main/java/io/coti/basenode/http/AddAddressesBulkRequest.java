package io.coti.basenode.http;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class AddAddressesBulkRequest extends Request implements ISignable, ISignValidatable {
    @NotEmpty(message = "Entities must not be empty")
    private List<AddressData> addresses;
    private Hash signerHash;
    private SignatureData signature;


    public AddAddressesBulkRequest(@NotEmpty(message = "Entities must not be empty") List<AddressData> addresses) {
        this.addresses = addresses;
    }

    public AddAddressesBulkRequest() {
        addresses = new ArrayList<>();
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

