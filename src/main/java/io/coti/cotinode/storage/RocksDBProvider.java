package io.coti.cotinode.storage;

import io.coti.cotinode.model.*;
import io.coti.cotinode.model.Interfaces.*;
import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import org.apache.commons.io.FileUtils;
import org.rocksdb.*;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Service
public class RocksDBProvider implements IPersistenceProvider {
    private RocksDB db;
    private final String logPath = ".\\rocksDB";
    private final String dbPath = ".\\rocksDB";
    private final List<String> columnFamilyClassNames = Arrays.asList(
            "DefaultColumnClassName",
            Transaction.class.getName(),
            BaseTransaction.class.getName(),
            Address.class.getName(),
            Balance.class.getName(),
            PreBalance.class.getName()
    );

    private Map<String, ColumnFamilyHandle> classNameToColumnFamilyHandleMapping = new LinkedHashMap<>();
    private List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
    List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();


    @Override
    public void init() {
        initiateColumnFamilyDescriptors();
        try {
            loadLibrary();
            createLogsPath();
            DBOptions options = new DBOptions();
            options.setCreateIfMissing(true);
            options.setCreateMissingColumnFamilies(true);
            db = RocksDB.open(options, dbPath, columnFamilyDescriptors, columnFamilyHandles);
            populateColumnFamilies();
        } catch (Exception e) {
            System.out.println("Error initiating Rocks DB");
            e.printStackTrace();
        }
    }

    private void populateColumnFamilies() {
        for (int i = 1; i < columnFamilyClassNames.size(); i++) {
            classNameToColumnFamilyHandleMapping.put(
                    columnFamilyClassNames.get(i), columnFamilyHandles.get(i));
        }
    }

    private void initiateColumnFamilyDescriptors() {
        columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY));
        for (int i = 1; i < columnFamilyClassNames.size(); i++) {
            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(columnFamilyClassNames.get(i).getBytes()));
        }
    }

    @Override
    public boolean put(IEntity entity) {
        try {
            db.put(
                    classNameToColumnFamilyHandleMapping.get(entity.getClass().getName()),
                    entity.getKey(),
                    SerializationUtils.serialize(entity));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ITransaction getTransaction(byte[] key) {
        return (ITransaction) get(Transaction.class, key);
    }

    @Override
    public IBaseTransaction getBaseTransaction(byte[] key) {
        return (IBaseTransaction) get(BaseTransaction.class, key);
    }

    @Override
    public IAddress getAddress(byte[] key) {
        return (IAddress) get(Address.class, key);
    }

    @Override
    public IBalance getBalance(byte[] key) {
        return (IBalance) get(Balance.class, key);
    }

    @Override
    public IPreBalance getPreBalance(byte[] key) {
        return (IPreBalance) get(PreBalance.class, key);
    }

    @Override
    public List<ITransaction> getAllTransactions() {
        return (List<ITransaction>) (List<?>) getAllEntities(Transaction.class);
    }

    @Override
    public void deleteTransaction(byte[] key) {
        delete(Transaction.class, key);
    }

    @Override
    public void deleteBaseTransaction(byte[] key) {
        delete(BaseTransaction.class, key);
    }

    @Override
    public void shutdown() {
        for (ColumnFamilyHandle columnFamilyHandle :
                columnFamilyHandles) {
            columnFamilyHandle.close();
        }
        db.close();
    }

    @Override
    public void deleteDatabaseFolder() {
        try {
            FileUtils.deleteDirectory(new File(dbPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<IEntity> getAllEntities(Class<?> entityClass) {
        RocksIterator iterator =
                db.newIterator(classNameToColumnFamilyHandleMapping.get(entityClass.getName()));
        iterator.seekToFirst();
        List<IEntity> entities = new ArrayList<>();

        while (iterator.isValid()) {
            entities.add((ITransaction) SerializationUtils.deserialize(iterator.value()));
            iterator.next();
        }

        return entities;
    }

    private IEntity get(Class<?> entityClass, byte[] key) {
        try {
            byte[] entityBytes = db.get(
                    classNameToColumnFamilyHandleMapping.get(entityClass.getName()), key);
            return (IEntity) SerializationUtils.deserialize(entityBytes);
        } catch (RocksDBException e) {
            e.printStackTrace();
            return null;
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

    private void delete(Class<?> entityClass, byte[] key) {
        try {
            db.delete(classNameToColumnFamilyHandleMapping.get(entityClass.getName()), key);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}