package io.coti.tools;

import io.coti.common.data.ConfirmationData;
import io.coti.common.data.TransactionData;
import io.coti.common.database.RocksDBConnector;
import io.coti.common.model.Transactions;
import io.coti.common.model.UnconfirmedTransactions;
import io.coti.common.services.ZeroSpendService;

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
        UnconfirmedTransactions unconfirmedTransactions = new UnconfirmedTransactions();
        unconfirmedTransactions.init();
        unconfirmedTransactions.databaseConnector = connector;

        for (TransactionData transactionData : zeroSpendService.getGenesisTransactions()) {
            transactions.put(transactionData);
            ConfirmationData confirmationData = new ConfirmationData(transactionData);
            unconfirmedTransactions.put(confirmationData);
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
