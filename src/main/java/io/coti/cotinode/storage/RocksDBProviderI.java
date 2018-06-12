package io.coti.cotinode.storage;
import io.coti.cotinode.model.Interfaces.IEntity;
import io.coti.cotinode.model.Transaction;
import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import org.rocksdb.*;
import org.springframework.stereotype.Service;
import org.apache.commons.io.FileUtils;
import org.springframework.util.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Service
public class RocksDBProviderI implements IPersistenceProvider {
    private RocksDB db;
    private final String logPath = ".\\rocksDB";
    private final String dbPath = ".\\rocksDB";
    private final List<String> columnFamilyClassNames = Arrays.asList(
            "DefaultColumnClassName",
            Transaction.class.getName()
    );
    private Map<String, ColumnFamilyHandle> classNameToColumnFamilyHandleMapping = new LinkedHashMap<>();
    private List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();

    @Override
    public void init() {
        columnFamilyHandles = new ArrayList<>();
        final List<ColumnFamilyDescriptor> columnFamilyDescriptors = new LinkedList<>();
        columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY));
        for(int i = 1; i < columnFamilyClassNames.size(); i++){
            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(columnFamilyClassNames.get(i).getBytes()));
        }
        try {
            loadLibrary();
            createLogsPath();
            DBOptions options = new DBOptions();
            options.setCreateIfMissing(true);
            options.setCreateMissingColumnFamilies(true);
            db = RocksDB.open(options, dbPath, columnFamilyDescriptors, columnFamilyHandles);
            for (int i = 1; i < columnFamilyClassNames.size(); i++) {
                classNameToColumnFamilyHandleMapping.put(
                        columnFamilyClassNames.get(i), columnFamilyHandles.get(i));
            }
        }
        catch (Exception e){
            System.out.println("Error initiating Rocks DB");
            e.printStackTrace();
        }
    }

    @Override
    public boolean put(IEntity entity) {
        try {
            db.put(
                    classNameToColumnFamilyHandleMapping.get(entity.getClass().getName()),
                    entity.getKey(),
                    SerializationUtils.serialize(entity)
            );
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Transaction> getAllTransactions() {
        RocksIterator iterator =
                db.newIterator(classNameToColumnFamilyHandleMapping.get(Transaction.class.getName()));
        iterator.seekToFirst();
        List<Transaction> transactions = new ArrayList<>();

        while(iterator.isValid()){
            transactions.add((Transaction)SerializationUtils.deserialize(iterator.value()));
            iterator.next();
        }

        return transactions;
    }


    @Override
    public Transaction getTransaction(byte[] key) {
        try {
            byte[] transactionBytes = db.get(
                    classNameToColumnFamilyHandleMapping.get(Transaction.class.getName()), key);
            return (Transaction)SerializationUtils.deserialize(transactionBytes);
        } catch (RocksDBException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteTransaction(byte[] key) {
        try {
            db.delete(classNameToColumnFamilyHandleMapping.get(Transaction.class.getName()), key);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        for (ColumnFamilyHandle columnFamilyHandle:
                columnFamilyHandles) {
            columnFamilyHandle.close();
        }
        db.close();
    }

    @Override
    public void deleteDatabaseFolder(){
        try {
            FileUtils.deleteDirectory(new File(dbPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createLogsPath() {
        File pathToLogDir = Paths.get(logPath).toFile();
        if (!pathToLogDir.exists() || !pathToLogDir.isDirectory()) {
            boolean success = pathToLogDir.mkdir();
            if (!success) {
                System.out.println("Unable to create new DB directory");
            }
        }
    }

    private void loadLibrary() {
        try {
            RocksDB.loadLibrary();
            System.out.println("RocksDB library loaded");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
