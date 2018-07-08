package io.coti.common.http;

import io.coti.common.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetBalancesRequest extends Request {
    @NotNull(message = "Addresses must not be blank")
    public List<Hash> addresses;
}