package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;

@Data
public class AddHistoryAddressesRequest extends Request implements ISignable, ISignValidatable {
    @NotEmpty(message = "Entities must not be empty")
    private Map<Hash, String> hashToAddressDataJsonMap;
    private Hash signerHash;
    private SignatureData signature;


    public AddHistoryAddressesRequest(@NotEmpty(message = "Entities must not be empty") Map<Hash, String> hashToAddressDataJsonMap) {
        this.hashToAddressDataJsonMap = hashToAddressDataJsonMap;
    }

    public AddHistoryAddressesRequest() {
        hashToAddressDataJsonMap = new HashMap<>();
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

