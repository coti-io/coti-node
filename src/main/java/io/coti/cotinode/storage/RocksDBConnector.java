package io.coti.cotinode.storage;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.IEntity;
import io.coti.cotinode.model.*;
import io.coti.cotinode.storage.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.*;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.ByteBuffer;
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
            //  db.getSnapshot()

            return db.get(classNameToColumnFamilyHandleMapping.get(columnFamilyName), hash.getBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
            return null;
        }
    }

    private RocksIterator getIterator(String coulumnFamilyName) {
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

    public Map<Object, Object> getFullMapFromDB(String columnFamilyName) {
        Map<Object, Object> balancesFromDB = new HashMap<>();
        try {
            RocksIterator iterator = getIterator(columnFamilyName);
            iterator.seekToFirst();
            while (iterator.isValid()) {
                balancesFromDB.put( SerializationUtils.deserialize(iterator.key()), SerializationUtils.deserialize(iterator.value()));

                iterator.next();
            }
        }
        catch (Exception ex){
            log.error("Exception while iterating on {}", columnFamilyName , ex);

        }
        return balancesFromDB;
    }

    public Map<Object,Object> getMapAfterKeyFromDB(String columnFamilyName, Object key){
        Map<Object, Object> balancesFromDB = new HashMap<>();
        boolean insert = false;
        try{
            RocksIterator iterator = getIterator(columnFamilyName);
            iterator.seekToFirst();
            while (iterator.isValid()) {
                if( SerializationUtils.deserialize(iterator.key()).equals(key)){
                    insert = true;
                }
                if(insert) {
                    balancesFromDB.put(SerializationUtils.deserialize(iterator.key()), SerializationUtils.deserialize(iterator.value()));
                }
                iterator.next();
            }
        }
        catch(Exception ex){
            log.error("Exception while iterating on {}", columnFamilyName , ex);
        }
        return balancesFromDB;
    }


    public RocksIterator getLastElementIteratorFromColumnFamily(String columnFamilyName){
        RocksIterator iterator = null;
        try{
            iterator = getIterator(columnFamilyName);
            iterator.seekToLast();

        }
        catch (Exception ex){
            log.error("Exception in getting the last element from a column family",ex);
        }
        return iterator;
    }

//    public Map<Hash, Double> getGapFromDB()

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