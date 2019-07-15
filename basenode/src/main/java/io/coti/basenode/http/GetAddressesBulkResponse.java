package io.coti.basenode.http;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class GetAddressesBulkResponse extends BulkResponse {
    private Map<Hash, AddressData> addressHashesToAddresses;

    public GetAddressesBulkResponse(Map<Hash,AddressData> addressHashesToAddresses,String message, String status) {
        super(message, status);
        this.addressHashesToAddresses = addressHashesToAddresses;

    }

    public GetAddressesBulkResponse(){
        this.addressHashesToAddresses = new LinkedHashMap<>();
    }


    public GetAddressesBulkResponse(Map<Hash, AddressData> addressHashesToAddresses) {
        this.addressHashesToAddresses = addressHashesToAddresses;
    }

    public GetAddressesBulkResponse(String message, String status){
        super(message, status);
        this.addressHashesToAddresses = new LinkedHashMap<>();
    }
}