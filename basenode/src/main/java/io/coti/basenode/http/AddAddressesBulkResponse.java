package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AddAddressesBulkResponse extends BulkResponse {

    private Map<Hash, Boolean> addressHashesToStoreResult;

    public AddAddressesBulkResponse(){
        this.addressHashesToStoreResult = new HashMap<>();
    }

    public AddAddressesBulkResponse(Map<Hash,Boolean> addressHashesToStoreResult, String message, String status) {
        super(message, status);
        this.addressHashesToStoreResult = addressHashesToStoreResult;
    }

    public AddAddressesBulkResponse(Map<Hash, Boolean> addressHashesToStoreResult) {
        this.addressHashesToStoreResult = addressHashesToStoreResult;
    }

    public AddAddressesBulkResponse(String message, String status) {
        super(message, status);
        this.addressHashesToStoreResult = new HashMap<>();
    }
}