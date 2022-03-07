package io.coti.basenode.http;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetTransactionByIndexRequest {

    @NotNull
    @Valid
    Long startingIndex;
    @Valid
    @NotNull
    Long endingIndex;
    private boolean extended;

}
