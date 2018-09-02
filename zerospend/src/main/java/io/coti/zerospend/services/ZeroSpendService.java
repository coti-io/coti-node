package io.coti.zerospend.services;

import io.coti.basenode.crypto.BaseTransactionWithPrivateKey;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.interfaces.IZeroSpendService;
import io.coti.zerospend.crypto.TransactionCyptoCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ZeroSpendService implements IZeroSpendService {
    private int currentHashCounter = 0;


    @Value("global.private.key")
    private static String GlobalPrivateKey = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b7412fbc3fabf82f8f";


    @Override
    public TransactionData getZeroSpendTransaction(double trustScore) {
        TransactionData transactionData = createZeroSpendTransaction(trustScore, "ZeroSpend");
        return transactionData;
    }

    @Override
    public List<TransactionData> getGenesisTransactions() {
        List<TransactionData> genesisTransactions = new LinkedList<>();
        for (int trustScore = 0; trustScore <= 100; trustScore = trustScore + 10) {
            TransactionData transactionData = createZeroSpendTransaction(trustScore, "Genesis");


            DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
            dspConsensusResult.setIndex(currentHashCounter);
            dspConsensusResult.setDspConsensus(true);
            transactionData.setDspConsensusResult(dspConsensusResult);

            genesisTransactions.add(transactionData);

            currentHashCounter = currentHashCounter + 1;
        }
        return genesisTransactions;
    }


    private TransactionData createZeroSpendTransaction(double trustScore, String description) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        BaseTransactionWithPrivateKey baseTransactionWithPrivateKey = new BaseTransactionWithPrivateKey(new BigDecimal(0), new Date(), GlobalPrivateKey);
        baseTransactions.add(baseTransactionWithPrivateKey);
        TransactionData transactionData = new TransactionData(baseTransactions, description, trustScore, new Date());
        transactionData.setSenderTrustScore(trustScore);
        transactionData.setAttachmentTime(new Date());
        transactionData.setZeroSpend(true);


        TransactionCyptoCreator transactionCryptoData = new TransactionCyptoCreator(transactionData);
        transactionCryptoData.signTransaction();
        return transactionData;
    }
}