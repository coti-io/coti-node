package io.coti.zero_spend.services.interfaces;

import io.coti.common.data.TransactionData;
import io.coti.common.http.GetZeroSpendTransactionsRequest;
import org.springframework.http.ResponseEntity;

public interface IGetZeroSpendTrxService {

    ResponseEntity<TransactionData> generateZeroSpendTrx(GetZeroSpendTransactionsRequest getZeroSpendTransactionsRequest);

}
