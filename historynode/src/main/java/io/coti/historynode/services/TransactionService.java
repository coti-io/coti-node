package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetEntitiesBulkRequest;
import io.coti.basenode.http.GetTransactionsResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.historynode.http.GetTransactionsRequest;
import io.coti.historynode.http.storageConnector.interaces.IStorageConnector;
import io.coti.historynode.model.AddressTransactionsByDatesHistories;
import io.coti.historynode.services.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TransactionService extends EntityService implements ITransactionService {

    @Autowired
    AddressTransactionsByDatesHistories addressTransactionsByDatesHistories;
    @Autowired
    private Transactions transactions;
    @Autowired
    private IStorageConnector storageConnector;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        endpoint = "/transactions";
    }

    @Override
    public ResponseEntity<IResponse> getTransactionsDetails(GetTransactionsRequest getTransactionRequest) {
        List<TransactionData> transactionsList = new ArrayList<>();
        List<Hash> hashList = new ArrayList<>();
        HistoryNodeConsensusResult historyNodeConsensusResult = new HistoryNodeConsensusResult();
        GetEntitiesBulkRequest getEntitiesBulkRequest = new GetEntitiesBulkRequest(hashList, historyNodeConsensusResult);

        List<Hash> transactionHashes = getTransactionsHashesByAddresses(getTransactionRequest);
        for (Hash transactionHash : transactionHashes) {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            if (transactionData == null) {
                getEntitiesBulkRequest.getHashes().add(transactionHash);
            } else {
                transactionsList.add(transactionData);
            }
        }

        transactionsList.addAll(getTransactionsDetailsFromStorage(getEntitiesBulkRequest));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GetTransactionsResponse(transactionsList));
    }


    @Override
    public void deleteLocalUnconfirmedTransactions() {
        transactions.forEach(transactionData -> {
            if (!transactionData.isTrustChainConsensus() || !transactionData.getDspConsensusResult().isDspConsensus()) {
                transactions.deleteByHash(transactionData.getHash());
            }
        });
    }

    private List<TransactionData> getTransactionsDetailsFromStorage(GetEntitiesBulkRequest getEntitiesBulkRequest) {
        List<TransactionData> transactionsList = new ArrayList<>();
        if (!getEntitiesBulkRequest.getHashes().isEmpty()) {
            ResponseEntity<IResponse> response
                    = storageConnector.getForObject(storageServerAddress + endpoint, ResponseEntity.class, getEntitiesBulkRequest);
            if (response.getStatusCode() == HttpStatus.OK) {
                //TODO: Convert http message body to transactions, and add to transactions
            }
        }
        return transactionsList;
    }

    private List<Hash> getTransactionsHashesByAddresses(GetTransactionsRequest getTransactionRequest) {
        List<Hash> transactionHashes = new ArrayList<>();
        long startDate = getTransactionRequest.getStartingDate() != null ? getTransactionRequest.getStartingDate().getTime() : Long.MIN_VALUE;
        long endDate = getTransactionRequest.getEndingDate() != null ? getTransactionRequest.getEndingDate().getTime() : Long.MAX_VALUE;
        getTransactionRequest.getAddressesHashes().forEach(addressHash ->
                transactionHashes.addAll(
                        addressTransactionsByDatesHistories.getByHash(addressHash)
                                .getTransactionsHistory().subMap(startDate, endDate).values()));
        return transactionHashes;
    }
}
