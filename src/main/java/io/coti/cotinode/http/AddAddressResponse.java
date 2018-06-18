package io.coti.cotinode.http;

import org.springframework.http.HttpStatus;

public class AddAddressResponse extends Response {
    public AddAddressResponse(HttpStatus status, String message) {
        super(status, message);
    }
}
