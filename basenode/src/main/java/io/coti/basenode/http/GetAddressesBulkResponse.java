package io.coti.basenode.http;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class GetAddressesBulkResponse extends GetBulkResponse {
    private Map<Hash, AddressData> addressHashesToAddresses;

    public GetAddressesBulkResponse(Map<Hash,AddressData> addressHashesToAddresses,String message, String status) {
        super(message, status);
        this.addressHashesToAddresses = addressHashesToAddresses;

    }

    public GetAddressesBulkResponse(){
        addressHashesToAddresses = new HashMap<>();
    }


    public GetAddressesBulkResponse(Map<Hash, AddressData> addressHashesToAddresses) {
        this.addressHashesToAddresses = addressHashesToAddresses;
    }
}