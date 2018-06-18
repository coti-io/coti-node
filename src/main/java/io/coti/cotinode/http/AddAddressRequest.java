package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;
import lombok.Data;

@Data
public class AddAddressRequest extends Request {
    public Hash addressHash;
}
