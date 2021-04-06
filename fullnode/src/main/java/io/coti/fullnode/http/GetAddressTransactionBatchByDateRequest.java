package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import io.coti.fullnode.http.data.TimeOrder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.Set;

@Data
public class GetAddressTransactionBatchByDateRequest extends Request {

    @NotNull(message = "Address Hashes must not be blank")
    private Set<@Valid Hash> addresses;
    private @Valid LocalDate startDate;
    private @Valid LocalDate endDate;
    private @Valid @Positive Integer limit;
    private @Valid TimeOrder order;
}
