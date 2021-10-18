package io.coti.financialserver.http;

import io.coti.basenode.http.interfaces.IRequest;
import io.coti.financialserver.data.ReceiverBaseTransactionOwnerData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class TransactionRequest implements IRequest {

    @NotNull
    private @Valid ReceiverBaseTransactionOwnerData receiverBaseTransactionOwnerData;
}
