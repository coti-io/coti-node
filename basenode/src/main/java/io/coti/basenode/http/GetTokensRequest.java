package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetTokensRequest extends Request {

    @NotNull(message = "Currency addresses must not be blank")
    public List<Hash> currencies;
}
