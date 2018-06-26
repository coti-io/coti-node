package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.IZeroSpendService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class ZeroSpendService implements IZeroSpendService {
    @Override
    public TransactionData getZeroSpendTransaction(double trustScore){
        return new TransactionData(new Hash("AAAA"), trustScore);
    }

    @Override
    public List<TransactionData> getGenesisTransactions() {
        List<TransactionData> genesisTransactions = new LinkedList<>();
        for(int trustScore = 0; trustScore <= 100; trustScore = trustScore + 10){
            genesisTransactions.add(new TransactionData(new Hash("AAAA"), trustScore));
        }
        return genesisTransactions;
    }
}
