package io.coti.basenode.services.interfaces;


import io.coti.basenode.data.TransactionData;

import java.util.Map;

public interface IPotService {

    void init();

    boolean validatePot(TransactionData transactionData);

    Map<String, Integer> executorSizes(Integer bucketNumber);

    void potAction(TransactionData transactionData);
}
