package io.coti.common.services;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.services.interfaces.IZeroSpendService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

@Service
public class ZeroSpendService implements IZeroSpendService {
    private int currentHashCounter = 0;

    @Override
    public List<TransactionData> getGenesisTransactions() {
        List<TransactionData> genesisTransactions = new LinkedList<>();
        for (int trustScore = 0; trustScore <= 100; trustScore = trustScore + 10) {
            TransactionData transactionData = new TransactionData(new Vector<>(), new Hash(currentHashCounter++), "Genesis", trustScore, new Date());
            transactionData.setZeroSpend(true);
            transactionData.setGenesis(true);
            transactionData.setAttachmentTime(new Date());
            genesisTransactions.add(transactionData);
            transactionData.setAttachmentTime(new Date());
        }
        return genesisTransactions;
    }
}
