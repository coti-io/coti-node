package io.coti.cotinode.storage;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.IEntity;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.*;
import io.coti.cotinode.storage.Interfaces.IDatabaseConnector;
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
public class RocksDBConnector implements IDatabaseConnector {
    private final String logPath = ".\\rocksDB";
    private final String dbPath = ".\\rocksDB";
    private final List<String> columnFamilyClassNames = Arrays.asList(
            "DefaultColumnClassName",
            Transactions.class.getName(),
            BaseTransactions.class.getName(),
            Addresses.class.getName(),
            Balances.class.getName(),
            PreBalances.class.getName()
    );
    private List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
    private RocksDB db;
    private Map<String, ColumnFamilyHandle> classNameToColumnFamilyHandleMapping = new LinkedHashMap<>();
    private List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();

    public RocksDBConnector() {
        log.info("RocksDB constructor running");
    }

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
    public byte[] getByHash(String columnFamilyName, Hash hash) {
        try {
            return db.get(classNameToColumnFamilyHandleMapping.get(columnFamilyName), hash.getBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean put(String columnFamilyName, IEntity entity) {
        try {
            db.put(
                    classNameToColumnFamilyHandleMapping.get(columnFamilyName),
                    entity.getKey().getBytes(),
                    SerializationUtils.serialize(entity));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void delete(String columnFamilyName, Hash key) {
        try {
            db.delete(classNameToColumnFamilyHandleMapping.get(columnFamilyName), key.getBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
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

}