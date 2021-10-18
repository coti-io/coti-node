package io.coti.basenode.data;


import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

@Data
public class ZeroSpendTransactionRequest implements IPropagatable {

    private static final long serialVersionUID = 1469470443784981284L;
    private Hash hash;
    private TransactionData transactionData;

}
