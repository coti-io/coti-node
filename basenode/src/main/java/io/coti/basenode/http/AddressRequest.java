package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddressRequest implements IRequest {
    @NotNull(message = "Address Hash must not be blank")
    private Hash address;
}
