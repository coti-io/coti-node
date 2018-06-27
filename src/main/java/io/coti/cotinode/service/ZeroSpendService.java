package io.coti.cotinode.service;

import com.fasterxml.jackson.databind.ser.Serializers;
import io.coti.cotinode.crypto.CryptoUtils;
import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.IZeroSpendService;
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
            BaseTransactionData btd = new BaseTransactionData("B1",10.0);
            TransactionData td = new TransactionData(new Hash(currentHashCounter++), trustScore);
            List<BaseTransactionData> baseTransactionsList = new LinkedList<>();
            baseTransactionsList.add(btd);
            td.setBaseTransactions(baseTransactionsList);
            genesisTransactions.add(td);
        }
        return genesisTransactions;
    }
}
