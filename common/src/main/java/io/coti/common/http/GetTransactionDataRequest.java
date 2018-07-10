package io.coti.common.http;

import io.coti.common.data.Hash;

import javax.validation.constraints.NotNull;

public class GetTransactionDataRequest extends Request {
    public Hash transactionHash;
    public int index;
}
