package io.coti.common.services;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.services.interfaces.IZeroSpendService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class ZeroSpendService implements IZeroSpendService {
    private int currentHashCounter = 0;
    @Override
    public TransactionData getZeroSpendTransaction(double trustScore) {
        return new TransactionData(new Hash(currentHashCounter++), trustScore);
    }

    @Override
    public List<TransactionData> getGenesisTransactions() {
        List<TransactionData> genesisTransactions = new LinkedList<>();
        for (int trustScore = 0; trustScore <= 100; trustScore = trustScore + 10) {
            TransactionData transactionData = new TransactionData(new Hash(currentHashCounter++), trustScore);
            transactionData.setZeroSpend(true);
            genesisTransactions.add(transactionData);
        }
        return genesisTransactions;
    }
}
