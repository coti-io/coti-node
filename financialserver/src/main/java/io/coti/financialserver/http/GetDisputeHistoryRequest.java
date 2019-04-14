package io.coti.financialserver.http;

import io.coti.financialserver.http.data.GetDisputeHistoryData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetDisputeHistoryRequest {

    @NotNull
    private @Valid GetDisputeHistoryData disputeHistoryData;

}
