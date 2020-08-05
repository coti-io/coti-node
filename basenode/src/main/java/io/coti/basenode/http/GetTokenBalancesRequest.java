package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTokenBalancesRequest extends Request {

    @NotNull(message = "Addresses must not be blank")
    private List<@Valid Hash> addresses;

}
