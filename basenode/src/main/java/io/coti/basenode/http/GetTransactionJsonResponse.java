package io.coti.basenode.http;


import io.coti.basenode.data.Hash;
import javafx.util.Pair;

public class GetTransactionJsonResponse extends BaseResponse {

    Pair<Hash, String> transaction;

    public GetTransactionJsonResponse(Hash transactionHash, String transactionJson) {
        this.transaction = new Pair<> (transactionHash, transactionJson);
    }
}
