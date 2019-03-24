package io.coti.basenode.http;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionResponseData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class GetSourcesResponse extends BaseResponse {
    private List<TransactionResponseData> sources;

    public GetSourcesResponse(List<TransactionData> sources) {
        super();
        this.sources = new ArrayList<>();
        sources.forEach(transactionData -> {
            try {
                this.sources.add(new TransactionResponseData(transactionData));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
