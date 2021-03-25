package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IRequest;
import io.coti.fullnode.http.data.TimeOrder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@Data
public class GetAddressTransactionBatchByDateRequest implements IRequest {

    @NotNull(message = "Address Hashes must not be blank")
    private Set<@Valid Hash> addresses;
    private @Valid LocalDate startDate;
    private @Valid LocalDate endDate;
    private @Valid TimeOrder order;
}
