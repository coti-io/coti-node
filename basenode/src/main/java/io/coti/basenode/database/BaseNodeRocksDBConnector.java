package io.coti.basenode.database;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.model.*;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class BaseNodeRocksDBConnector implements IDatabaseConnector {
    @Value("${database.folder.name}")
    private String databaseFolderName;
    protected List<String> columnFamilyClassNames;

    @Value("${application.name}")
    private String applicationName;
    @Value("${resetDatabase}")
    private boolean resetDatabase;
    private String dbPath;
    private List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
    private RocksDB db;
    private Map<String, ColumnFamilyHandle> classNameToColumnFamilyHandleMapping = new LinkedHashMap<>();
    private List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();


    private void deleteDatabaseFolder() {
        File index = new File(dbPath);
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


    public void init() {
        setColumnFamily();
        init(applicationName + databaseFolderName);
        log.info("{} is up", this.getClass().getSimpleName());
    }


    protected void setColumnFamily() {
        columnFamilyClassNames = new ArrayList<>(Arrays.asList(
                "DefaultColumnClassName",
                Transactions.class.getName(),
                Addresses.class.getName(),
                AddressTransactionsHistories.class.getName(),
                TransactionIndexes.class.getName(),
                TransactionVotes.class.getName()
        ));

    }

    public void init(String dbPath) {
        this.dbPath = dbPath;
        if (resetDatabase) {
            deleteDatabaseFolder();
        }
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

    public RocksIterator getIterator(String columnFamilyName) {
        RocksIterator it = null;
        try {
            ReadOptions readOptions = new ReadOptions();

            ColumnFamilyHandle columnFamilyHandler = classNameToColumnFamilyHandleMapping.get(columnFamilyName);
            it = db.newIterator(columnFamilyHandler, readOptions);
            if (columnFamilyHandler == null) {
                log.error("Column family {} iterator wasn't found ", columnFamilyName);
            }
        } catch (Exception ex) {
            log.error("Exception while getting iterator of {}", columnFamilyName, ex);
        }
        return it;
    }

    @Override
    public boolean isEmpty(String columnFamilyName) {
        RocksIterator iterator = getIterator(columnFamilyName);
        iterator.seekToFirst();
        return !iterator.isValid();
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

    public void shutdown() {
        log.info("Shutting down {}", this.getClass().getSimpleName());
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
        File pathToLogDir = Paths.get(dbPath).toFile();
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