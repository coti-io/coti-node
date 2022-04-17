package io.coti.basenode.http;

import io.coti.basenode.data.TransactionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;
@Data
@EqualsAndHashCode(callSuper = true)
public class GetTokenHistoryResponse extends BaseResponse {

    private Set<TransactionData> transactions;
}
