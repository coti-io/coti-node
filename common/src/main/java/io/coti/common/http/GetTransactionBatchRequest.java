package io.coti.common.http;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetTransactionBatchRequest implements Serializable {
    public long startingIndex;

    private GetTransactionBatchRequest(){}
    public GetTransactionBatchRequest(long startingIndex){
        this.startingIndex = startingIndex;
    }
}
