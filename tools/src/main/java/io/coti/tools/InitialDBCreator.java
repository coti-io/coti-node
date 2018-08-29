package io.coti.tools;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionIndexData;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.ZeroSpendService;

import java.io.File;

public class InitialDBCreator {
    public static void main(String[] args) {
        System.out.println("Deleting initialDB folder...");
        deleteInitialDatabaseFolder();
        RocksDBConnector connector = new RocksDBConnector();
        connector.init("initialDatabase");
        ZeroSpendService zeroSpendService = new ZeroSpendService();
        Transactions transactions = new Transactions();
        transactions.init();
        transactions.databaseConnector = connector;
        TransactionIndexes transactionIndexes = new TransactionIndexes();
        transactionIndexes.init();
        transactionIndexes.databaseConnector = connector;

        byte[] accumulatedHash = "GENESIS".getBytes();
        for (TransactionData transactionData : zeroSpendService.getGenesisTransactions()) {
            transactions.put(transactionData);
            accumulatedHash = TransactionIndexService.getAccumulatedHash(accumulatedHash, transactionData.getHash(), transactionData.getDspConsensusResult().getIndex());
            transactionIndexes.put(new TransactionIndexData(transactionData.getHash(), transactionData.getDspConsensusResult().getIndex(), accumulatedHash));
        }
    }

    private static void deleteInitialDatabaseFolder() {
        File index = new File("initialDatabase");
        if (!index.exists()) {
            return;
        }
        String[] entries = index.list();
        for (String s : entries) {
            File currentFile = new File(index.getPath(), s);
            currentFile.delete();
        }
        index.delete();
    }
}
