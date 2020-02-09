package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetTrustScoreRequest extends Request {

    @NotNull
    private Hash userHash;
}
