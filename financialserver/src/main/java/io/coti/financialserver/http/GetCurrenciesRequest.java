package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetCurrenciesRequest extends Request {

    @NotNull(message = "Token hashes must not be blank")
    public List<@Valid Hash> tokenHashes;
}
