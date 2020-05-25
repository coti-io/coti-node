package io.coti.financialserver.http;

import io.coti.basenode.http.interfaces.IRequest;
import io.coti.financialserver.http.data.GetDisputesData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetDisputesRequest implements IRequest {

    @NotNull
    private @Valid GetDisputesData disputesData;
}
