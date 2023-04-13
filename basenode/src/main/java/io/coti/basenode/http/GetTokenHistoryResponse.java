package io.coti.basenode.http;

import io.coti.basenode.http.data.TransactionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTokenHistoryResponse extends BaseResponse {

    private Set<TransactionResponseData> transactions;

    public GetTokenHistoryResponse() {
        super();
    }

    public GetTokenHistoryResponse(Set<TransactionResponseData> transactions) {
        this.transactions = transactions;
    }
}
