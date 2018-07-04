package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;

import static io.coti.cotinode.http.HttpStringConstants.ADDRESS_CREATED_MESSAGE;

public class AddAddressResponse extends Response {
    public AddAddressResponse(Hash addressHash, String message) {
        super(String.format(message, addressHash));
    }

    public AddAddressResponse(Hash addressHash, String message, String status) {
        super(String.format(message, addressHash),status);
    }
}
