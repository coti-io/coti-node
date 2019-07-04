package io.coti.zerospend.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class SetIndexesResponse extends BaseResponse {

    private int requestedIndexNumber;
    private int indexedTransactionNumber;

    public SetIndexesResponse(int requestedIndexNumber, int indexedTransactionNumber) {
        this.requestedIndexNumber = requestedIndexNumber;
        this.indexedTransactionNumber = indexedTransactionNumber;
    }
}
