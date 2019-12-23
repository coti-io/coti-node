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
    @Value("${reset.transactions}")
    private boolean resetTransactions;
    @Autowired
    private ApplicationContext ctx;
    protected String dbPath;
    protected RocksDB db;
    protected List<String> columnFamilyClassNames;
    protected List<String> resetColumnFamilyNames;
    protected Map<String, ColumnFamilyHandle> classNameToColumnFamilyHandleMapping = new LinkedHashMap<>();

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
        resetColumnFamilyNames = new ArrayList<>(Arrays.asList(
                Transactions.class.getName(),
                AddressTransactionsHistories.class.getName(),
                TransactionIndexes.class.getName()
        ));

    }

    public void init(String dbPath) {
        this.dbPath = dbPath;
        try {
            initColumnFamilyClasses();
            loadLibrary();
            createDbDirectory();
            if (dropNotListedColumnFamilies) {
                openDBAndDropNotListedColumnFamilies();
            } else {
                openDB();
            }
            if (resetTransactions) {
                resetColumnFamilies();
            }
        } catch (DataBaseException e) {
            throw new DataBaseException("Error initiating Rocks DB.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataBaseException("Error initiating Rocks DB.", e);
        }
    }

    private void openDBAndDropNotListedColumnFamilies() {
        try {
            List<String> dbColumnFamilyNames = getColumnFamilyNamesFromDB();
            List<String> notListedColumnFamilyNames = getNotListedColumnNames(dbColumnFamilyNames);
            columnFamilyClassNames.forEach(columnFamilyClassName -> {
                if (!dbColumnFamilyNames.contains(columnFamilyClassName)) {
                    dbColumnFamilyNames.add(columnFamilyClassName);
                }
            });
            openDB(dbColumnFamilyNames);
            if (!notListedColumnFamilyNames.isEmpty()) {
                dropColumnFamilies(notListedColumnFamilyNames);
            }
        } catch (DataBaseException e) {
            throw new DataBaseException("Error at dropping not listed Rocks DB column families.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataBaseException("Error at dropping not listed Rocks DB column families.", e);
        }
    }

    protected List<String> getColumnFamilyNamesFromDB() {
        try (Options options = new Options()) {
            List<byte[]> columnFamilies = RocksDB.listColumnFamilies(options, dbPath);
            return columnFamilies.stream().map(dbColumnFamily -> new String(dbColumnFamily)).collect(Collectors.toList());
        } catch (Exception e) {
            throw new DataBaseException(String.format("Error at getting column families.", e));
        }
    }

    private List<String> getNotListedColumnNames(List<String> dbColumnFamilyNames) {
        return dbColumnFamilyNames.stream().filter(dbColumnFamilyName ->
                !dbColumnFamilyName.equals(new String(RocksDB.DEFAULT_COLUMN_FAMILY)) && !columnFamilyClassNames.contains(dbColumnFamilyName)
        ).collect(Collectors.toList());
    }

    private void resetColumnFamilies() {
        resetColumnFamilies(null);
    }

    private void resetColumnFamilies(List<String> resetColumnFamilyNames) {
        resetColumnFamilyNames = Optional.ofNullable(resetColumnFamilyNames).orElse(this.resetColumnFamilyNames);
        dropAndCreateColumnFamilies(resetColumnFamilyNames, true);
    }

    private void dropColumnFamilies(List<String> dropColumnFamilies) {
        dropAndCreateColumnFamilies(dropColumnFamilies, false);
    }

    private void dropAndCreateColumnFamilies(List<String> columnFamilyNames, boolean create) {
        columnFamilyNames.forEach(columnFamilyName -> {
            ColumnFamilyHandle columnFamilyHandle = classNameToColumnFamilyHandleMapping.get(columnFamilyName);
            try {
                if (columnFamilyHandle != null) {
                    db.dropColumnFamily(columnFamilyHandle);
                    columnFamilyHandle.close();
                    classNameToColumnFamilyHandleMapping.remove(columnFamilyName);
                    if (create) {
                        columnFamilyHandle = db.createColumnFamily(new ColumnFamilyDescriptor(columnFamilyName.getBytes()));
                        classNameToColumnFamilyHandleMapping.put(columnFamilyName, columnFamilyHandle);
                        log.info("Column family {} reset", columnFamilyName);
                    } else {
                        log.info("Column family {} dropped", columnFamilyName);
                    }
                }
            } catch (Exception e) {
                throw new DataBaseException("Error resetting column families.", e);
            }
        });
    }

    protected void openDB() {
        openDB(null);
    }

    private void openDB(List<String> dbColumnFamilies) {
        try (DBOptions dbOptions = new DBOptions()) {

            List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
            initiateColumnFamilyDescriptors(dbColumnFamilies, columnFamilyDescriptors);
            dbOptions.setCreateIfMissing(true);
            dbOptions.setCreateMissingColumnFamilies(true);
            db = RocksDB.open(dbOptions, dbPath, columnFamilyDescriptors, columnFamilyHandles);
            populateColumnFamilies(dbColumnFamilies, columnFamilyHandles);
        } catch (Exception e) {
            throw new DataBaseException("Error opening Rocks DB.", e);
        }

    }

    protected void initColumnFamilyClasses() {
        for (int i = 1; i < columnFamilyClassNames.size(); i++) {
            try {
                ((Collection) ctx.getBean(Class.forName(columnFamilyClassNames.get(i)))).init();
            } catch (Exception e) {
                throw new DataBaseException("Error at init column family classes.", e);
            }
        }
    }

    private void populateColumnFamilies(List<String> dbColumnFamilyNames, List<ColumnFamilyHandle> columnFamilyHandles) {
        List<String> columnFamilyNamesToPopulate = Optional.ofNullable(dbColumnFamilyNames).orElse(columnFamilyClassNames);
        for (int i = 1; i < columnFamilyNamesToPopulate.size(); i++) {
            classNameToColumnFamilyHandleMapping.put(
                    columnFamilyNamesToPopulate.get(i), columnFamilyHandles.get(i));
        }
    }

    private void initiateColumnFamilyDescriptors(List<String> dbColumnFamilies, List<ColumnFamilyDescriptor> columnFamilyDescriptors) {
        List<String> columnFamilyNamesToInit = Optional.ofNullable(dbColumnFamilies).orElse(columnFamilyClassNames);
        columnFamilyNamesToInit.forEach(columnFamilyName -> columnFamilyDescriptors.add(new ColumnFamilyDescriptor(columnFamilyName.getBytes())));
    }

    @Override
    public void generateDataBaseBackup(String backupPath) {
        log.info("Starting database backup to {}", backupPath);
        try (BackupableDBOptions backupableDBOptions = new BackupableDBOptions(backupPath);
             BackupEngine rocksBackupEngine = BackupEngine.open(Env.getDefault(), backupableDBOptions)) {
            rocksBackupEngine.createNewBackup(db, false);
            log.info("Finished database backup to {}", backupPath);
        } catch (Exception e) {
            throw new DataBaseException("Failed to generate database backup.", e);
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
        } catch (DataBaseException e) {
            throw new DataBaseException("Failed to restore database.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new DataBaseException("Failed to restore database", e);
        }
    }

    private void checkIfBackupHasNotListedColumnFamilies() {
        List<String> dbColumnFamilyNames = getColumnFamilyNamesFromDB();
        List<String> notListedColumnFamilyNames = getNotListedColumnNames(dbColumnFamilyNames);
        if (!notListedColumnFamilyNames.isEmpty()) {
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

    protected void createDbDirectory() {
        try {
            File dbDirectory = Paths.get(dbPath).toFile();
            if (!dbDirectory.exists() || !dbDirectory.isDirectory()) {
                boolean success = dbDirectory.mkdir();
                if (!success) {
                    throw new DataBaseException("Unable to create new DB directory");
                }
            }
        } catch (DataBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DataBaseException("Create db directory error.", e);

        }
    }

    protected void loadLibrary() {
        try {
            log.info("Starting to load RocksDB library");
            RocksDB.loadLibrary();
            log.info("RocksDB library loaded");
        } catch (Exception e) {
            throw new DataBaseException("Error loading rocksdb library", e);
        }
    }

    protected void closeDB() {
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