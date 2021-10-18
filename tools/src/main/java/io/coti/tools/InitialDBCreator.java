package io.coti.tools;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
public class InitialDBCreator {
    public static void main(String[] args) {
        try {
            log.error("Deleting initialDB folder...");
            deleteInitialDatabaseFolder();
            BaseNodeRocksDBConnector connector = new BaseNodeRocksDBConnector();
            connector.init("initialDatabase");

            Transactions transactions = new Transactions();
            transactions.init();
            transactions.databaseConnector = connector;
            TransactionIndexes transactionIndexes = new TransactionIndexes();
            transactionIndexes.init();
            transactionIndexes.databaseConnector = connector;
        } catch (Exception e) {
            log.error("Error at initial db creator", e);
        }
    }

    private static void deleteInitialDatabaseFolder() throws IOException {
        File index = new File("initialDatabase");
        if (!index.exists()) {
            return;
        }
        String[] entries = index.list();
        if (entries != null) {
            for (String s : entries) {
                File currentFile = new File(index.getPath(), s);
                Files.delete(currentFile.toPath());
                log.info("File {} deleted", currentFile.getName());
            }

            Files.delete(index.toPath());
            log.info("File {} deleted", index.getName());

        }
    }
}
