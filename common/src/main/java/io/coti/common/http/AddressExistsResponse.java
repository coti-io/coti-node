package io.coti.common.http;


import io.coti.common.data.Hash;



public class AddressExistsResponse extends Response {

    public AddressExistsResponse(Hash addressHash, String message) {
        super(String.format(message, addressHash));
    }
}
