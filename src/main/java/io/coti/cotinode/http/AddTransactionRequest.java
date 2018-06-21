package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class AddTransactionRequest extends Request {
    @NotNull
    public List<Map.Entry<Hash, Double>> transferredAmounts;
    @NotNull
    public Hash transactionHash;
}
