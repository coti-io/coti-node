package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetTrustScoreRequest implements IRequest {

    @NotNull
    private Hash userHash;
}
