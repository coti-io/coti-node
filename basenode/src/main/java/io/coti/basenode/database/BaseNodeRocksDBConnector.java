package io.coti.basenode.database;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.DataBaseException;
import io.coti.basenode.model.*;
import io.coti.basenode.model.Collection;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.File;
import java.lang.reflect.Constructor;
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
    private String dbPath;
    private List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
    private Map<String, ColumnFamilyHandle> classNameToColumnFamilyHandleMapping = new LinkedHashMap<>();
    private List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();

    private RocksDB db;
    private BackupEngine rocksBackupEngine;
    private BackupableDBOptions backupableDBOptions;

    public void init() {
        setColumnFamily();
        init(applicationName + databaseFolderName);
        //init(applicationName + databaseFolderName+ "/backups/shared" ); //backup

        String backupFolderPath = applicationName + databaseFolderName+ "/backups";
        createBackupFolderIfDoesntExsit(backupFolderPath);
        backupableDBOptions = new BackupableDBOptions(backupFolderPath);
        try {
            rocksBackupEngine = BackupEngine.open(Env.getDefault(), backupableDBOptions);
        } catch (RocksDBException e) {
            log.error("Failed to open backup engine");
            e.printStackTrace();
        }
        log.info("Created folder and backup engine");
        log.info("{} is up", this.getClass().getSimpleName());

    }

    public void backUpDb(){
        // Not thread safe!!!!
        log.info("Trying to back up database");
        try {

            //TODO 8/11/2019 astolia: not sure about true or false
            rocksBackupEngine.createNewBackup(db, false);
            rocksBackupEngine.createNewBackup(db, true);
            //rocksBackupEngine.close();

            log.info("db backup finished. number of backed up databases: {}", rocksBackupEngine.getBackupInfo().size());
        } catch (RocksDBException e) {
            log.info("db backup failed");
            e.printStackTrace();
        }
    }

    private void createBackupFolderIfDoesntExsit(String backupFolderPath){
        File directory = new File(backupFolderPath);
        if (! directory.exists()){
            directory.mkdir();
        }
    }

    public void restoreUpDb(){
        log.info("Trying to restore database");
        try {

            db.close();
            db = null;
            String backupFolderPath = applicationName + databaseFolderName+ "/backups";
            createBackupFolderIfDoesntExsit(backupFolderPath);
            backupableDBOptions = new BackupableDBOptions(backupFolderPath);
            rocksBackupEngine = BackupEngine.open(Env.getDefault(), backupableDBOptions);
            //rocksBackupEngine = BackupEngine.open(Env.getDefault(), backupableDBOptions);

            //TODO 8/11/2019 astolia: true or false?
            RestoreOptions restoreOpt = new RestoreOptions(true);
            //TODO 8/11/2019 astolia: maybe need to get backup id and add it as first argument
//            rocksBackupEngine.restoreDbFromBackup(1,applicationName + databaseFolderName,applicationName + databaseFolderName,restoreOpt);
            rocksBackupEngine.restoreDbFromLatestBackup(applicationName + databaseFolderName, applicationName + databaseFolderName, restoreOpt);
            rocksBackupEngine.close();


            DBOptions options = new DBOptions();
            options.setCreateIfMissing(true);
            options.setCreateMissingColumnFamilies(true);
            options.setEnv(Env.getDefault());
            columnFamilyHandles = new ArrayList<>();
            db = RocksDB.open(options, dbPath, columnFamilyDescriptors, columnFamilyHandles);
            populateColumnFamilies();


//            db = RocksDB.open(new Options().setCreateIfMissing(true), applicationName + databaseFolderName);
            log.info("db restore finished");
        } catch (RocksDBException e) {
            log.info("db restore failed");
            e.printStackTrace();
        }
    }


    protected void setColumnFamily() {
        columnFamilyClassNames = new ArrayList<>(Arrays.asList(
                "DefaultColumnClassName",
                Transactions.class.getName(),
                Addresses.class.getName(),
                AddressTransactionsHistories.class.getName(),
                TransactionIndexes.class.getName(),
                TransactionVotes.class.getName(),
                NodeRegistrations.class.getName()
        ));

    }

    public void init(String dbPath) {
        this.dbPath = dbPath;

        initColumnFamilyClasses();
        try {
            initiateColumnFamilyDescriptors();
            loadLibrary();
            createLogsPath();
            DBOptions options = new DBOptions();
            options.setCreateIfMissing(true);
            options.setCreateMissingColumnFamilies(true);
            db = RocksDB.open(options, dbPath, columnFamilyDescriptors, columnFamilyHandles);
            populateColumnFamilies();
        } catch (Exception e) {
            restoreUpDb();
            //throw new DataBaseException(String.format("Error initiating Rocks DB. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
        }
    }

    private void initColumnFamilyClasses() {
        for (int i = 1; i < columnFamilyClassNames.size(); i++) {
            try {
                ((Constructor<? extends Collection<? extends IEntity>>) Class.forName(columnFamilyClassNames.get(i)).getConstructor()).newInstance().init();
            } catch (Exception e) {
                throw new DataBaseException(String.format("Error at init column family classes. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
            }
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
            db.put(classNameToColumnFamilyHandleMapping.get(columnFamilyName), key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean put(String columnFamilyName, WriteOptions writeOptions, byte[] key, byte[] value) {
        try {
            db.put(classNameToColumnFamilyHandleMapping.get(columnFamilyName), writeOptions, key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean put(String columnFamilyName, WriteBatch writeBatch, byte[] key, byte[] value) {
        try {
            writeBatch.put(classNameToColumnFamilyHandleMapping.get(columnFamilyName), key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean putBatch(WriteBatch writeBatch) {
        try {
            db.write(new WriteOptions(), writeBatch);
            return true;
        } catch (RocksDBException e) {
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