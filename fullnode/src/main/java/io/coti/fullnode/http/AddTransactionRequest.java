package io.coti.fullnode.http;

import io.coti.basenode.data.*;
import io.coti.basenode.http.Request;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public class AddTransactionRequest extends Request {
    @NotEmpty
    private List<@Valid BaseTransactionData> baseTransactions;
    @NotNull
    public Hash hash;
    @NotEmpty
    public String transactionDescription;
    @NotNull
    public Instant createTime;
    @NotEmpty
    private List<@Valid TransactionTrustScoreData> trustScoreResults;
    @NotNull
    public Hash senderHash;
    @NotNull
    public @Valid SignatureData senderSignature;
    @NotNull
    public TransactionType type;

    public List<TransactionTrustScoreData> getTrustScoreResults() {
        return trustScoreResults;
    }

    public List<BaseTransactionData> getBaseTransactions() {
        return baseTransactions;
    }
}


