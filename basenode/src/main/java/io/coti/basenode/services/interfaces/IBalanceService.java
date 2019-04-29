package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public interface IBalanceService {

    void init();

    boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas);

    ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest);

    void continueHandleBalanceChanges(Hash addressHash);

    void rollbackBaseTransactions(TransactionData transactionData);

    void validateBalances();

    void updateBalanceFromClusterStamp(Hash addressHash, BigDecimal amount) throws Exception;

    void updatePreBalanceFromClusterStamp();

    void updateBalance(Hash addressHash, BigDecimal amount);

    void updatePreBalance(Hash addressHash, BigDecimal amount);
}