package io.coti.common.http;

import io.coti.common.data.Hash;

public class AddAddressResponse extends Response {
    public AddAddressResponse(Hash addressHash, String message) {
        super(String.format(message, addressHash));
    }

    public AddAddressResponse(Hash addressHash, String message, String status) {
        super(String.format(message, addressHash),status);
    }
}
