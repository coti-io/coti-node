package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class ReplaceNodeRequest {

    @NotNull
    @Valid
    private Hash updatedNodeHash;
    @NotNull
    @Valid
    private Hash existingNodeHash;
    @NotNull
    @Valid
    private String webServerUrl;
}
