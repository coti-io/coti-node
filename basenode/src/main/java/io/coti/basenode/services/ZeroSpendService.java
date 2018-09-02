package io.coti.basenode.services;

import io.coti.basenode.crypto.BaseTransactionCryptoWrapper;
import io.coti.basenode.crypto.BaseTransactionWithPrivateKey;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.interfaces.IZeroSpendService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ZeroSpendService implements IZeroSpendService {
    private int currentHashCounter = 0;


    @Value("global.private.key")
    private static String GlobalPrivateKey;



    @Override
    public TransactionData getZeroSpendTransaction(double trustScore) {
        TransactionData transactionData = createZeroSpendTransaction(trustScore,"ZeroSpend");
        return transactionData;
    }

    @Override
    public List<TransactionData> getGenesisTransactions() {
        List<TransactionData> genesisTransactions = new LinkedList<>();
        for (int trustScore = 0; trustScore <= 100; trustScore = trustScore + 10) {
            TransactionData transactionData =  createZeroSpendTransaction(trustScore,"Genesis");
            genesisTransactions.add(transactionData);
        }
        return genesisTransactions;
    }




    private TransactionData createZeroSpendTransaction(double trustScore, String description){
        List<BaseTransactionData> baseTransactions =  new ArrayList<>();
        BaseTransactionWithPrivateKey baseTransactionWithPrivateKey = new BaseTransactionWithPrivateKey(new BigDecimal(0), new Date(),GlobalPrivateKey);
        baseTransactions.add(baseTransactionWithPrivateKey);
        TransactionData transactionData = new TransactionData(baseTransactions,description,trustScore,new Date());
        transactionData.setSenderTrustScore(trustScore);
        transactionData.setAttachmentTime(new Date());
        transactionData.setZeroSpend(true);



        TransactionCrypto transactionCryptoData = new TransactionCrypto();
        transactionCryptoData.signMessage(transactionData);

        return transactionData;
    }
}
