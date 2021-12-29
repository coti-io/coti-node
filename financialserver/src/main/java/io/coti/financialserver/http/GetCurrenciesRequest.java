package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetCurrenciesRequest extends Request {

    @NotNull(message = "Token hashes must not be blank")
    private List<@Valid Hash> tokenHashes;
}
