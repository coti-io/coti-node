package io.coti.basenode.http;

import io.coti.basenode.data.Hash;

public class GetTransactionTrustScoreRequest extends Request {
    public Hash userHash;
    public Hash transactionHash;
}
