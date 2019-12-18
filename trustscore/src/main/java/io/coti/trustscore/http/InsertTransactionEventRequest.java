package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class InsertTransactionEventRequest extends Request {

    @NotNull
    private Date eventDate;

    @NotNull
    private Hash userHash;

    private SignatureData signature;

    private TransactionData transactionData;

}