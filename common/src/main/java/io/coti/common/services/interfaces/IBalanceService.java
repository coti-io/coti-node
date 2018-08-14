package io.coti.common.services.interfaces;

import io.coti.common.data.*;
import io.coti.common.http.GetBalancesRequest;
import io.coti.common.http.GetBalancesResponse;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IBalanceService {

    boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas);

    ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest);

    void rollbackBaseTransactions(TransactionData transactionData);

    void insertSavedTransaction(TransactionData transactionData);

    void finalizeInit();

    void setTccToTrue(TccInfo tccInfo);

    void setDspcToTrue(DspConsensusResult dspConsensusResult);

}