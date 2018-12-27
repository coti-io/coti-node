package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.ReceiverBaseTransactionOwnerData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class TransactionRequest extends Request {

    @NotNull
    private @Valid ReceiverBaseTransactionOwnerData receiverBaseTransactionOwnerData;
}
