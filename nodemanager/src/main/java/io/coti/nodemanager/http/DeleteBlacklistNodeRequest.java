package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class DeleteBlacklistNodeRequest {

    @NotNull
    @Valid
    private Hash nodeHash;
}
