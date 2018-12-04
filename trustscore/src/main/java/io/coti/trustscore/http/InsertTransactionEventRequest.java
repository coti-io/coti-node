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
    public Date eventDate;

    @NotNull
    public Hash userHash;

    //@NotNull
    public SignatureData signature;

    public TransactionData transactionData;

}