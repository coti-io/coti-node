package io.coti.common.http;

import io.coti.common.data.Hash;

public class GetTransactionTrustScoreRequest extends Request {
    public Hash userHash;
    public Hash transactionHash;
}
