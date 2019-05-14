package io.coti.zerospend.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SetTransactionIndexRequest extends BaseResponse {

    @NotEmpty
    private Hash transactionHash;

}
