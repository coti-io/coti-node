package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddressRequest extends Request {
    @NotNull(message = "Address Hash must not be blank")
    private Hash address;
}
