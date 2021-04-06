package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetAddressTransactionBatchRequest extends Request {

    @NotEmpty(message = "Addresses must not be blank")
    private List<@Valid Hash> addresses;
}
