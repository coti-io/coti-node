package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IBalanceService {

    void init() throws Exception;

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

    void shutdown();
}