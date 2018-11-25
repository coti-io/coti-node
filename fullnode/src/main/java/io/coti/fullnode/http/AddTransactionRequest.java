package io.coti.fullnode.http;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionTrustScoreData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.http.Request;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class AddTransactionRequest extends Request {
    @NotEmpty
    public List<@Valid BaseTransactionData> baseTransactions;
    @NotNull
    public Hash hash;
    @NotEmpty
    public String transactionDescription;
    @NotNull
    public Date createTime;
    @NotEmpty
    public List<@Valid TransactionTrustScoreData> trustScoreResults;
    @NotNull
    public Hash senderHash;
    @NotNull
    public TransactionType type;
}


