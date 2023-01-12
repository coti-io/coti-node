package io.coti.basenode.http;

import io.coti.basenode.data.ReceiverBaseTransactionOwnerData;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class TransactionRequest implements IRequest {

    @NotNull
    private @Valid ReceiverBaseTransactionOwnerData receiverBaseTransactionOwnerData;
}
