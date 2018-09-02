package io.coti.basenode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.interfaces.IZeroSpendService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ZeroSpendService implements IZeroSpendService {


    @PostConstruct
    public void init(){




    }


    @Override
    public TransactionData getZeroSpendTransaction(double trustScore) {
        return null;
    }

    @Override
    public List<TransactionData> getGenesisTransactions() {
        return null;
    }
}
