package io.coti.financialserver.http;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.DisputeItemData;

@Data
public class ItemRequest extends Request {

    @NotNull
    private @Valid DisputeItemData disputeItemData;
}
