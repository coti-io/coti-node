package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetBalancesRequest implements IRequest {

    @NotNull(message = "Addresses must not be blank")
    private List<Hash> addresses;

}