package io.coti.common.http;

import io.coti.common.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddressBulkRequest {


    @NotNull(message = "Address Hashes must not be blank")
    public Hash[] addresses;

    public AddressBulkRequest(){

    }
}
