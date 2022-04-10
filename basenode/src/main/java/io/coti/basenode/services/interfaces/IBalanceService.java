package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import io.coti.basenode.http.GetTokenBalancesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public interface IBalanceService {

    void init();

    boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactions);

    ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest);

    ResponseEntity<IResponse> getTokenBalances(GetTokenBalancesRequest getTokenBalancesRequest);

    void continueHandleBalanceChanges(Hash addressHash, Hash currencyHash);

    void rollbackBaseTransactions(TransactionData transactionData);

    void validateBalances();

    void updateBalanceAndPreBalanceFromClusterStamp(Hash addressHash, Hash currencyHash, BigDecimal amount);

    void updateBalance(Hash addressHash, Hash currencyHash, BigDecimal amount);

    void updatePreBalance(Hash addressHash, Hash currencyHash, BigDecimal amount);

    BigDecimal getBalance(Hash addressHash, Hash currencyHash);

    BigDecimal getPreBalance(Hash addressHash, Hash currencyHash);
}