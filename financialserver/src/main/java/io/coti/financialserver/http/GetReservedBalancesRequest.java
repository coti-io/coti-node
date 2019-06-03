package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class GetReservedBalancesRequest {

    @NotEmpty
    private List<@Valid Hash> addresses;
}
