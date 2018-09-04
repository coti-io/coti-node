package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddressRequest extends Request {
    @NotNull(message = "Address Hash must not be blank")
    private Hash address;
}
