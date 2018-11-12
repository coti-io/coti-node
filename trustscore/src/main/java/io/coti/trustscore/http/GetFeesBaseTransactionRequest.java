package io.coti.trustscore.http;
import io.coti.trustscore.http.data.FeeBaseTransactionData;
import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class GetFeesBaseTransactionRequest {

    @NotNull
    public FeeBaseTransactionData fullNodeFeeBaseTransactionData;
}
