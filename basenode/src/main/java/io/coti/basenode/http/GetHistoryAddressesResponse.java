package io.coti.basenode.http;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class GetHistoryAddressesResponse extends Response implements ISignValidatable, ISignable {
    @NotEmpty
    private Map<Hash, AddressData> addressHashesToAddresses;
    @NotNull
    private SignatureData signature;
    @NotNull
    private Hash signerHash;

    public GetHistoryAddressesResponse(Map<Hash,AddressData> addressHashesToAddresses, String message, String status) {
        super(message, status);
        this.addressHashesToAddresses = addressHashesToAddresses;

    }

    public GetHistoryAddressesResponse(){
        this.addressHashesToAddresses = new LinkedHashMap<>();
    }


    public GetHistoryAddressesResponse(Map<Hash, AddressData> addressHashesToAddresses) {
        this.addressHashesToAddresses = addressHashesToAddresses;
    }

    public GetHistoryAddressesResponse(String message, String status){
        super(message, status);
        this.addressHashesToAddresses = new LinkedHashMap<>();
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