package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.IZeroSpendService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ZeroSpendService implements IZeroSpendService {
    @Override
    public TransactionData getZeroSpendTransaction(double trustScore){
        return new TransactionData(new Hash("AAAA"));
    }

    @Override
    public List<TransactionData> getGenesisTransactions() {
        return Collections.emptyList();
    }
}
