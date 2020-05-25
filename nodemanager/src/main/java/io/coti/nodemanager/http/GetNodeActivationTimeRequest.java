package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetNodeActivationTimeRequest implements IRequest {

    @NotNull
    private @Valid Hash nodeHash;
}
