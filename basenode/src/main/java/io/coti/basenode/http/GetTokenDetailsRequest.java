package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTokenDetailsRequest extends Request {

    @NotNull
    private @Valid Hash currencyHash;

}
