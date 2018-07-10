package io.coti.common.http;

import io.coti.common.data.TransactionData;
import lombok.Data;

@Data
public class GetLastIndexResponse extends Response {

    private int lastIndex;
    public  GetLastIndexResponse (int lastIndex) {
        super();
        this.lastIndex = lastIndex;
    }
}