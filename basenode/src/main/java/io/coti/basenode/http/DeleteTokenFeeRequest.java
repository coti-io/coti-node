package io.coti.basenode.http;

import io.coti.basenode.data.NodeFeeType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DeleteTokenFeeRequest {

    @NotNull
    @NotEmpty
    String tokenSymbol;
    @Valid
    NodeFeeType nodeFeeType;
}
