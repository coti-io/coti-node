package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;

import static io.coti.cotinode.http.HttpStringConstants.ADDRESS_CREATED_MESSAGE;

public class AddAddressResponse extends Response {
    public AddAddressResponse(Hash addressHash) {
        super(String.format(ADDRESS_CREATED_MESSAGE, addressHash));
    }
}
