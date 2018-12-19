package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.http.data.GetDisputesData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetDisputesRequest extends Request {

    @NotNull
    private @Valid GetDisputesData disputesData;
}
