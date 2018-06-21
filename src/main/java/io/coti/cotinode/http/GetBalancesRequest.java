package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetBalancesRequest extends Request {
    @NotNull(message = "Addresses must not be blank")
    public List<Hash> addresses;
}