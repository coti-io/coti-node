package io.coti.common.services;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.services.interfaces.IZeroSpendService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class ZeroSpendService implements IZeroSpendService {
    private int currentHashCounter = 0;
    @Override
    public TransactionData getZeroSpendTransaction(double trustScore) {
        TransactionData transactionData =  new TransactionData(null, new Hash(currentHashCounter++) , "ZeroSpend", trustScore);
        transactionData.setAttachmentTime(new Date(0));
        return transactionData;
    }

    @Override
    public List<TransactionData> getGenesisTransactions() {
        List<TransactionData> genesisTransactions = new LinkedList<>();
        for (int trustScore = 0; trustScore <= 100; trustScore = trustScore + 10) {
            TransactionData transactionData = new TransactionData(null, new Hash(currentHashCounter++) ,"Genesis", trustScore);
            transactionData.setZeroSpend(true);
            transactionData.setAttachmentTime(new Date());
            genesisTransactions.add(transactionData);
            transactionData.setAttachmentTime(new Date(0));
        }
        return genesisTransactions;
    }
}
