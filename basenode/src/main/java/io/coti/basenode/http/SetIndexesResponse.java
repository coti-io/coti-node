package io.coti.basenode.http;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetIndexesResponse extends BaseResponse {

    private int requestedIndexNumber;
    private int indexedTransactionNumber;

    public SetIndexesResponse(int requestedIndexNumber, int indexedTransactionNumber) {
        this.requestedIndexNumber = requestedIndexNumber;
        this.indexedTransactionNumber = indexedTransactionNumber;
    }
}
