package io.coti.basenode.database;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.DataBaseException;
import io.coti.basenode.model.Collection;
import io.coti.basenode.model.*;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaseNodeRocksDBConnector implements IDatabaseConnector {

    @Value("${database.folder.name}")
    private String databaseFolderName;
    @Value("${application.name}")
    private String applicationName;
    @Value("${db.drop.column.families}")
    private boolean dropNotListedColumnFamilies;
    @Autowired
    private ApplicationContext ctx;
    private String dbPath;
    private RocksDB db;
    protected List<String> columnFamilyClassNames;
    private Map<String, ColumnFamilyHandle> classNameToColumnFamilyHandleMapping = new LinkedHashMap<>();

    public void init() {
        setColumnFamily();
        init(applicationName + databaseFolderName);
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public String getDBPath() {
        return dbPath;
    }

    protected void setColumnFamily() {
        columnFamilyClassNames = new ArrayList<>(Arrays.asList(
                new String(RocksDB.DEFAULT_COLUMN_FAMILY),
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
        try {
            initColumnFamilyClasses();
            loadLibrary();
            createDbDirectory();
            if (dropNotListedColumnFamilies) {
                dropNotListedColumnFamilies();
            }
            openDB();
        } catch (Exception e) {
            if (e instanceof DataBaseException) {
                throw new DataBaseException("Error initiating Rocks DB. " + e.getMessage());
            }
            throw new DataBaseException(String.format("Error initiating Rocks DB. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
        }
    }

    private void dropNotListedColumnFamilies() {
        try {
            List<byte[]> dbColumnFamilies = getColumnFamiliesFromDB();
            List<String> notListedColumnFamilies = getNotListedColumnFamilies(dbColumnFamilies);

            if (!notListedColumnFamilies.isEmpty()) {
                openDB(dbColumnFamilies);
                for (String notListedColumnFamily : notListedColumnFamilies)
                    db.dropColumnFamily(classNameToColumnFamilyHandleMapping.get(notListedColumnFamily));
                closeDB();
            }
        } catch (Exception e) {
            if (e instanceof DataBaseException) {
                throw new DataBaseException("Error at dropping not listed Rocks DB column families. " + e.getMessage());
            }
            throw new DataBaseException(String.format("Error at dropping not listed Rocks DB column families. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
        }
    }

    private List<byte[]> getColumnFamiliesFromDB() {
        try (Options options = new Options()) {
            return RocksDB.listColumnFamilies(options, dbPath);
        } catch (Exception e) {
            throw new DataBaseException(String.format("Error at getting column families. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
        }
    }

    private List<String> getNotListedColumnFamilies(List<byte[]> dbColumnFamilies) {
        return dbColumnFamilies.stream().map(dbColumnFamily -> new String(dbColumnFamily)).filter(dbColumnFamilyName ->
                !dbColumnFamilyName.equals(new String(RocksDB.DEFAULT_COLUMN_FAMILY)) && !columnFamilyClassNames.contains(dbColumnFamilyName)
        ).collect(Collectors.toList());
    }

    private void openDB() {
        openDB(null);
    }

    private void openDB(List<byte[]> dbColumnFamilies) {
        try (DBOptions dbOptions = new DBOptions()) {

            List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
            initiateColumnFamilyDescriptors(dbColumnFamilies, columnFamilyDescriptors);
            dbOptions.setCreateIfMissing(true);
            dbOptions.setCreateMissingColumnFamilies(true);
            db = RocksDB.open(dbOptions, dbPath, columnFamilyDescriptors, columnFamilyHandles);
            populateColumnFamilies(dbColumnFamilies, columnFamilyHandles);
        } catch (Exception e) {
            throw new DataBaseException(String.format("Error opening Rocks DB. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
        }

    }

    private void initColumnFamilyClasses() {
        for (int i = 1; i < columnFamilyClassNames.size(); i++) {
            try {
                ((Collection) ctx.getBean(Class.forName(columnFamilyClassNames.get(i)))).init();
            } catch (Exception e) {
                throw new DataBaseException(String.format("Error at init column family classes. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
            }
        }
    }

    private void populateColumnFamilies(List<byte[]> dbColumnFamilies, List<ColumnFamilyHandle> columnFamilyHandles) {
        if (dbColumnFamilies == null) {
            for (int i = 1; i < columnFamilyClassNames.size(); i++) {
                classNameToColumnFamilyHandleMapping.put(
                        columnFamilyClassNames.get(i), columnFamilyHandles.get(i));
            }
        } else {
            for (int i = 1; i < dbColumnFamilies.size(); i++) {
                classNameToColumnFamilyHandleMapping.put(
                        new String(dbColumnFamilies.get(i)), columnFamilyHandles.get(i));
            }
        }
    }

    private void initiateColumnFamilyDescriptors(List<byte[]> dbColumnFamilies, List<ColumnFamilyDescriptor> columnFamilyDescriptors) {
        if (dbColumnFamilies == null) {
            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY));
            for (int i = 1; i < columnFamilyClassNames.size(); i++) {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(columnFamilyClassNames.get(i).getBytes()));
            }
        } else {
            dbColumnFamilies.forEach(dbColumnFamily -> columnFamilyDescriptors.add(new ColumnFamilyDescriptor(dbColumnFamily)));
        }
    }

    @Override
    public void generateDataBaseBackup(String backupPath) {
        log.info("Starting database backup to {}", backupPath);
        try (BackupableDBOptions backupableDBOptions = new BackupableDBOptions(backupPath);
             BackupEngine rocksBackupEngine = BackupEngine.open(Env.getDefault(), backupableDBOptions)) {
            rocksBackupEngine.createNewBackup(db, false);
            log.info("Finished database backup to {}", backupPath);
        } catch (Exception e) {
            throw new DataBaseException(String.format("Failed to generate database backup. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
        }
    }

    @Override
    public void restoreDataBase(String backupPath) {
        log.info("Starting database restore from {}", backupPath);
        try (BackupableDBOptions backupableDBOptions = new BackupableDBOptions(backupPath);
             BackupEngine rocksBackupEngine = BackupEngine.open(Env.getDefault(), backupableDBOptions);
             RestoreOptions restoreOpt = new RestoreOptions(false)) {

            closeDB();
            rocksBackupEngine.restoreDbFromLatestBackup(applicationName + databaseFolderName, applicationName + databaseFolderName, restoreOpt);
            checkIfBackupHasNotListedColumnFamilies();
            openDB();
            log.info("Finished database restore from {}", backupPath);
        } catch (Exception e) {
            if (e instanceof DataBaseException) {
                throw new DataBaseException("Failed to restore database. " + e.getMessage());
            }
            throw new DataBaseException(String.format("Failed to restore database. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
        }
    }

    private void checkIfBackupHasNotListedColumnFamilies() {
        List<byte[]> dbColumnFamilies = getColumnFamiliesFromDB();
        List<String> notListedColumnFamilies = getNotListedColumnFamilies(dbColumnFamilies);
        if (!notListedColumnFamilies.isEmpty()) {
            throw new DataBaseException("The backup database has column families that are not listed in the code. Please check your version");
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

    private void createDbDirectory() {
        try {
            File dbDirectory = Paths.get(dbPath).toFile();
            if (!dbDirectory.exists() || !dbDirectory.isDirectory()) {
                boolean success = dbDirectory.mkdir();
                if (!success) {
                    throw new DataBaseException("Unable to create new DB directory");
                }
            }
        } catch (Exception e) {
            if (e instanceof DataBaseException) {
                throw e;
            }
            throw new DataBaseException(String.format("Create db directory error. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
        }
    }

    private void loadLibrary() {
        try {
            log.info("Starting to load RocksDB library");
            RocksDB.loadLibrary();
            log.info("RocksDB library loaded");
        } catch (Exception e) {
            throw e;
        }
    }

    private void closeDB() {
        Iterator<ColumnFamilyHandle> iterator = classNameToColumnFamilyHandleMapping.values().iterator();
        while (iterator.hasNext()) {
            ColumnFamilyHandle columnFamilyHandle = iterator.next();
            columnFamilyHandle.close();
            iterator.remove();
        }
        db.close();
        db = null;
    }

    @Override
    public void shutdown() {
        log.info("Shutting down {}", this.getClass().getSimpleName());
        closeDB();
    }

}