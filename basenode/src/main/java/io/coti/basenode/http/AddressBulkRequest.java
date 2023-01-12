package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AddressBulkRequest {

    @NotNull(message = "Address Hashes must not be blank")
    public List<@Valid Hash> addresses;

}
