package io.coti.cotinode.storage;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.model.*;
import io.coti.cotinode.model.Interfaces.*;
import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.rocksdb.*;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
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
    @PostConstruct
    public void init() {
        log.info("Initializing RocksDB");
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
            log.error("Error initiating Rocks DB");
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
                    entity.getKey().getBytes(),
                    SerializationUtils.serialize(entity));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Transaction getTransaction(Hash key) {
        return (Transaction) get(Transaction.class, key);
    }

    @Override
    public BaseTransaction getBaseTransaction(Hash key) {
        return (BaseTransaction) get(BaseTransaction.class, key);
    }

    @Override
    public Address getAddress(Hash key) {
        return (Address) get(Address.class, key);
    }

    @Override
    public Balance getBalance(Hash key) {
        return (Balance) get(Balance.class, key);
    }

    @Override
    public PreBalance getPreBalance(Hash key) {
        return (PreBalance) get(PreBalance.class, key);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return (List<Transaction>) (List<?>) getAllEntities(Transaction.class);
    }

    @Override
    public void deleteTransaction(Hash key) {
        delete(Transaction.class, key);
    }

    @Override
    public void deleteBaseTransaction(Hash key) {
        delete(BaseTransaction.class, key);
    }

    @PreDestroy
    @Override
    public void shutdown() {
        log.info("Shutting down rocksDB");
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
            entities.add((Transaction) SerializationUtils.deserialize(iterator.value()));
            iterator.next();
        }

        return entities;
    }

    private IEntity get(Class<?> entityClass, Hash key) {
        try {
            Hash entityHash = new Hash(db.get(
                    classNameToColumnFamilyHandleMapping.get(entityClass.getName()), key.getBytes()));
            return (IEntity) SerializationUtils.deserialize(entityHash.getBytes());
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
                log.error("Unable to create new DB directory");
            }
        }
    }

    private void loadLibrary() {
        try {
            RocksDB.loadLibrary();
            log.info("RocksDB library loaded");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void delete(Class<?> entityClass, Hash key) {
        try {
            db.delete(classNameToColumnFamilyHandleMapping.get(entityClass.getName()), key.getBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}