package io.coti.basenode.http;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class GetAddressesResponse extends Response implements ISignValidatable {
    @NotEmpty
    private Map<Hash,AddressData> addressHashesToAddresses;
    @NotNull
    private Hash userHash;
    @NotNull
    private SignatureData userSignature;

    public GetAddressesResponse(Map<Hash,AddressData> addressHashesToAddresses,String message, String status) {
        super(message, status);
        this.addressHashesToAddresses = addressHashesToAddresses;

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