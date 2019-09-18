package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

@Data
public class GenerateTokenRequest extends Request {

    private Hash transactionHash;

}
