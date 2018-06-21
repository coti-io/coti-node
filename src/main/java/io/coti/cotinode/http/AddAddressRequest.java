package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddAddressRequest extends Request {
    @NotNull(message = "Address Hash must not be blank")
    public Hash addressHash;
}
