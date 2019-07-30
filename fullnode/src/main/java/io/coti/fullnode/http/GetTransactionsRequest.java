package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.util.List;

@Data
public class GetTransactionsRequest {

    private List<Hash> transactionHashes;
}
