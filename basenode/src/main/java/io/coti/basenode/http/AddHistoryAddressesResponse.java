package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AddHistoryAddressesResponse extends Response implements ISignValidatable, ISignable {

    @NotEmpty
    private Map<Hash, Boolean> addressHashesToStoreResult;

    private SignatureData signature;

    private Hash signerHash;

    public AddHistoryAddressesResponse() {
        addressHashesToStoreResult = new HashMap<>();
    }

    public AddHistoryAddressesResponse(Map<Hash, Boolean> addressHashesToStoreResult, String message, String status) {
        super(message, status);
        this.addressHashesToStoreResult = addressHashesToStoreResult;
    }

    public AddHistoryAddressesResponse(Map<Hash, Boolean> addressHashesToStoreResult) {
        this.addressHashesToStoreResult = addressHashesToStoreResult;
    }

    public AddHistoryAddressesResponse(String message, String status) {
        super(message, status);
        this.addressHashesToStoreResult = new LinkedHashMap<>();
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