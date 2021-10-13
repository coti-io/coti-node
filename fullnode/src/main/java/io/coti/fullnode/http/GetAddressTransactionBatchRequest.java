package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class GetAddressTransactionBatchRequest extends Request {

    @NotEmpty(message = "Addresses must not be blank")
    private List<@Valid Hash> addresses;
    @Valid
    private boolean extended;
}
