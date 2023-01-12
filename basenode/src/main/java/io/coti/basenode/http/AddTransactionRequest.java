package io.coti.basenode.http;

import io.coti.basenode.data.*;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Data
public class AddTransactionRequest implements IRequest {

    @NotEmpty
    private List<@Valid BaseTransactionData> baseTransactions;
    @NotNull
    private @Valid Hash hash;
    @NotEmpty
    private String transactionDescription;
    @NotNull
    private Instant createTime;
    @NotEmpty
    private List<@Valid TransactionTrustScoreData> trustScoreResults;
    @NotNull
    private Hash senderHash;
    @NotNull
    private @Valid SignatureData senderSignature;
    @NotNull
    private TransactionType type;

}


