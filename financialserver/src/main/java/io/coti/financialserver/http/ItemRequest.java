package io.coti.financialserver.http;

import io.coti.basenode.http.interfaces.IRequest;
import io.coti.financialserver.data.DisputeItemData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class ItemRequest implements IRequest {

    @NotNull
    private @Valid DisputeItemData disputeItemData;
}
