package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.ResolveRecourseClaim;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class RecourseClaimRequest extends Request {

    @NotNull
    private @Valid ResolveRecourseClaim resolveRecourseClaim;
}
