package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;

public interface IConfirmationService {
    void init();

    void insertSavedTransaction(TransactionData transactionData);

    void insertMissingTransaction(TransactionData transactionData);

    void setTccToTrue(TccInfo tccInfo);

    void setDspcToTrue(DspConsensusResult dspConsensusResult);

    long getTotalConfirmed();

    long getTccConfirmed();

    long getDspConfirmed();

    void shutdown();
}
