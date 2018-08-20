package io.coti.common.services.interfaces;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.DspConsensusResult;
import io.coti.common.data.TccInfo;
import io.coti.common.data.TransactionData;
import io.coti.common.http.GetBalancesRequest;
import io.coti.common.http.GetBalancesResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IBalanceService {

    boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas);

    ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest);

    void rollbackBaseTransactions(TransactionData transactionData);

    void insertSavedTransaction(TransactionData transactionData);

    void finalizeInit();

    void setTccToTrue(TccInfo tccInfo);

    void setDspcToTrue(DspConsensusResult dspConsensusResult);

    long getTotalConfirmed();

    long getTccConfirmed();

    long getDspConfirmed();
}