package io.coti.common.services.interfaces;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.ConfirmationData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.http.GetBalancesRequest;
import io.coti.common.http.GetBalancesResponse;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IBalanceService {

    boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas);

    void insertToUnconfirmedTransactions(ConfirmationData confirmationData);

    ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest);

    Map<Hash, BigDecimal> getBalanceMap();

    Map<Hash, BigDecimal> getPreBalanceMap();

    void rollbackBaseTransactions(TransactionData transactionData);
}

