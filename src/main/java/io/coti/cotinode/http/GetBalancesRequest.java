package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;
import lombok.Data;

import java.util.List;

@Data
public class GetBalancesRequest extends Request {
    public List<Hash> addresses;
}
