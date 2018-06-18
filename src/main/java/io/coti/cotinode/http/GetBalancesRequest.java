package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class GetBalancesRequest extends Request {
    @NotBlank(message = "Addresses must not be null!")
    public List<Hash> addresses;
}
