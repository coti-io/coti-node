package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;
import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public interface IBalanceService {

    void init() throws Exception;

    boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas);

    ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest);

    void continueHandleBalanceChanges(Hash addressHash);

    void rollbackBaseTransactions(TransactionData transactionData);

    void finalizeInit();

    void updateBalance(Hash addressHash, BigDecimal amount);

    void updatePreBalance(Hash addressHash, BigDecimal amount);
}