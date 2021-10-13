package io.coti.fullnode.http;

import io.coti.basenode.http.data.ExtendedTransactionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetExtendedTransactionResponse extends GetTransactionResponse {

    public GetExtendedTransactionResponse(ExtendedTransactionResponseData extendedTransactionResponseData) {
        super(extendedTransactionResponseData);
    }
}
