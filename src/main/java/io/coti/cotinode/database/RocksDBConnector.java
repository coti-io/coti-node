package io.coti.cotinode.database;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.interfaces.IEntity;
import io.coti.cotinode.model.*;
import io.coti.cotinode.database.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
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
            ConfirmedTransactions.class.getName(),
            UnconfirmedTransactions.class.getName()
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
    public byte[] getByKey(String columnFamilyName, byte[] key) {
        try {
            return db.get(classNameToColumnFamilyHandleMapping.get(columnFamilyName), key);
            } catch (RocksDBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public RocksIterator getIterator(String coulumnFamilyName) {
        RocksIterator it = null;
        try {
            ReadOptions readOptions = new ReadOptions();

            ColumnFamilyHandle coulumnFamilyHandler = classNameToColumnFamilyHandleMapping.get(coulumnFamilyName);
            it = db.newIterator(coulumnFamilyHandler, readOptions);
            if (coulumnFamilyHandler == null) {
                log.error("Column family {} iterator wasn't found ", coulumnFamilyName);
            }
        } catch (Exception ex) {
            log.error("Exception while getting iterator of {}", coulumnFamilyName,ex);
        }
        return it;
    }



    @Override
    public boolean put(String columnFamilyName, byte[] key, byte[] value) {
        try {
            db.put(
                    classNameToColumnFamilyHandleMapping.get(columnFamilyName),
                    key,
                    value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void delete(String columnFamilyName, byte[] key) {
        try {
            db.delete(classNameToColumnFamilyHandleMapping.get(columnFamilyName), key);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
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