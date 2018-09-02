package io.coti.tools;

import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;

import java.io.File;

public class InitialDBCreator {
    public static void main(String[] args) {
        System.out.println("Deleting initialDB folder...");
        deleteInitialDatabaseFolder();
        RocksDBConnector connector = new RocksDBConnector();
        connector.init("initialDatabase");

        Transactions transactions = new Transactions();
        transactions.init();
        transactions.databaseConnector = connector;
        TransactionIndexes transactionIndexes = new TransactionIndexes();
        transactionIndexes.init();
        transactionIndexes.databaseConnector = connector;
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
