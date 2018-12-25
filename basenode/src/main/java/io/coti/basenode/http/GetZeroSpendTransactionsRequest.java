package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class GetZeroSpendTransactionsRequest {


    private Hash fullNodeHash;


}
