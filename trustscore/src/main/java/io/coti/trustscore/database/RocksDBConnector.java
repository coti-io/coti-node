package io.coti.trustscore.database;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.exceptions.DataBaseException;
import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.Buckets.BucketInitialTrustScoreEventsData;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.data.UserTrustScoreData;
import io.coti.trustscore.data.parameters.TransactionUserParameters;
import io.coti.trustscore.data.scorebuckets.*;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.data.scoreenums.ScoreType;
import io.coti.trustscore.data.scoreevents.KYCDocumentScoreData;
import io.coti.trustscore.data.Events.BalanceCountAndContribution;
import io.coti.trustscore.data.Events.InitialTrustScoreData;
import io.coti.trustscore.data.parameters.BalanceAndContribution;
import io.coti.trustscore.data.parameters.DocumentDecayedContributionData;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.model.*;
import io.coti.trustscore.utils.DatesCalculation;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Primary
@Service
public class RocksDBConnector extends BaseNodeRocksDBConnector {

    @Override
    protected void setColumnFamily() {
        super.setColumnFamily();
        columnFamilyClassNames.addAll(Arrays.asList(
                UserTrustScores.class.getName(),    //  new
                Buckets.class.getName(),            //  new
                MerchantRollingReserveAddresses.class.getName(),
                UnlinkedAddresses.class.getName(),
                AddressUserIndex.class.getName()
        ));
    }

    private void addToColumnFamily() {
        columnFamilyClassNames.addAll(Arrays.asList(
                BucketEvents.class.getName(),       //  old - delete in the next version
                TrustScores.class.getName()        //  old - delete in the next version
        ));
    }

    @Override
    public void init(String dbPath) {
//        super.init(dbPath);

        super.dbPath = dbPath;
        boolean refactoringDB;

        try {
            super.loadLibrary();
            super.createDbDirectory();

            List<String> dbColumnFamilies = super.getColumnFamilyNamesFromDB();

            refactoringDB = dbColumnFamilies.stream().anyMatch(dbColumnFamilyName -> dbColumnFamilyName.equals(TrustScores.class.getName()));

            if (refactoringDB) {
                addToColumnFamily();
            }
            super.initColumnFamilyClasses();

            super.openDB();
        } catch (Exception e) {
            if (e instanceof DataBaseException) {
                throw new DataBaseException("Error initiating Rocks DB. " + e.getMessage());
            }
            throw new DataBaseException(String.format("Error initiating Rocks DB. Class: %s, Exception message: %s", e.getClass(), e.getMessage()));
        }

        if (refactoringDB) {
            if (!isEmpty(UserTrustScores.class.getName())) {
                log.info("The column 'UserTrustScores' is not empty while 'TrustScore' column exists. The node is stopped for the DB is not correct.");
                System.exit(0);
            } else {
                databaseAdHocRefactor();
                log.info("Database reorganization finished");

                databaseDeleteColumnData(BucketEvents.class.getName()); // delete it if no BucketEvents in the next version
                databaseDeleteColumnData(TrustScores.class.getName()); // delete it if no TrustScores in the next version
                databaseDropColumn(BucketEvents.class.getName()); // delete it if no BucketEvents in the next version
                databaseDropColumn(TrustScores.class.getName()); // delete it if no TrustScores in the next version

//                log.info("Please restart the node");
//                System.exit(0);
            }
        }
    }

    private void databaseDeleteColumnData(String Name) {
        RocksIterator iterator;

        try {
            iterator = getIterator(Name);
            iterator.seekToFirst();
            if (iterator.isValid()) {
                byte[] start = iterator.key();
                iterator.seekToLast();
                byte[] end = iterator.key();
                iterator.close();
                db.deleteRange(classNameToColumnFamilyHandleMapping.get(Name), start, end);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    private void databaseDropColumn(String Name) {
        try {
            db.dropColumnFamily(classNameToColumnFamilyHandleMapping.get(Name));
            classNameToColumnFamilyHandleMapping.get(Name).close();
            classNameToColumnFamilyHandleMapping.remove(Name);
            columnFamilyClassNames.remove(Name);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    private void databaseCreateColumn(String Name, boolean initCollection) {
        ColumnFamilyHandle handle = null;

        try {
            columnFamilyClassNames.add(Name);
            if (initCollection) {
                try {
                    ((Constructor<? extends Collection<? extends IEntity>>) Class.forName(Name).getConstructor()).newInstance().init();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            ColumnFamilyDescriptor columnFamilyDescriptor = new ColumnFamilyDescriptor(Name.getBytes());
            handle = db.createColumnFamily(columnFamilyDescriptor);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        classNameToColumnFamilyHandleMapping.put(Name, handle);
    }

//  Temporary section begin

    private void createBucketsForUser(UserTrustScoreData usertrustScoreData, BucketDocumentScoreData bucketDocumentScoreData, BucketTransactionScoreData bucketTransactionScoreData) {
        try {
            for (ScoreType score : ScoreType.values()) {

                BucketData bucketData;
                switch (score) {
                    case DOCUMENT_SCORE:
                        bucketData = bucketDocumentScoreData;
                        break;
                    case TRANSACTION:
                        bucketData = bucketTransactionScoreData;
                        break;
                    default:
                        bucketData = (BucketData) score.bucket.getDeclaredConstructor().newInstance();
                        bucketData.setUserType(usertrustScoreData.getUserType());
                        bucketData.setHash(getBucketHashByUserHashAndScoreType(usertrustScoreData.getHash(), score.value));
                }

                this.put(Buckets.class.getName(), bucketData.getHash().getBytes(), SerializationUtils.serialize(bucketData));

                usertrustScoreData.getEventTypeToBucketHashMap().put(score, bucketData.getHash());
            }
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            log.error(e.toString());
        }
    }

    private Hash getBucketHashByUserHashAndScoreType(Hash userHash, int scoreType) {
        return new Hash(ByteBuffer.allocate(userHash.getBytes().length + Integer.BYTES).
                put(userHash.getBytes()).putInt(scoreType).array());
    }

    private TransactionUserParameters userParametersAdHoc(String userType) {
        TransactionUserParameters userParameters = new TransactionUserParameters();
        switch (UserType.enumFromString(userType)) {
            case CONSUMER:
            case ARBITRATOR:
                userParameters.setSemiDecay(1095);
                userParameters.setWeight(1);
                userParameters.setBalanceLevel08(40000);
                userParameters.setBalanceSemiDecay(30);
                break;
            case MERCHANT:
                userParameters.setSemiDecay(365);
                userParameters.setWeight(1);
                userParameters.setBalanceLevel08(40000);
                userParameters.setBalanceSemiDecay(30);
                break;
            case FULL_NODE:
                userParameters.setSemiDecay(30);
                userParameters.setWeight(0.4);
                break;
            case DSP_NODE:
            case TRUST_SCORE_NODE:
                userParameters.setSemiDecay(365000);
                userParameters.setWeight(0.4);
                break;
        }
        return userParameters;
    }

    private void databaseAdHocRefactor() {
        RocksIterator iterator;

        iterator = getIterator(TrustScores.class.getName());
        iterator.seekToFirst();
        while (iterator.isValid()) {
            TrustScoreData trustScoreData = (TrustScoreData) SerializationUtils.deserialize(iterator.value());

            UserTrustScoreData userTrustScoreData = new UserTrustScoreData(trustScoreData.getUserHash(), trustScoreData.getUserType().toString());
            LocalDateTime trustScoreDataCreateTime = LocalDateTime.ofInstant(trustScoreData.getCreateTime().toInstant(), ZoneId.of("Asia/Jerusalem"));
            userTrustScoreData.setCreateTime(trustScoreDataCreateTime);
            LocalDate trustScoreDataCreateDate = LocalDate.of(trustScoreData.getCreateTime().getYear() + 1900,
                    trustScoreData.getCreateTime().getMonth() + 1, trustScoreData.getCreateTime().getDate());
            userTrustScoreData.setZeroTrustFlag(trustScoreData.getZeroTrustFlag());

            Hash bucketDocumentScoreOld = trustScoreData.getEventTypeToBucketHashMap().get(EventType.INITIAL_EVENT);
            Hash bucketDocumentScoreNew = getBucketHashByUserHashAndScoreType(userTrustScoreData.getHash(), ScoreType.DOCUMENT_SCORE.value);

            BucketInitialTrustScoreEventsData bucketInitialTrustScoreEventsData =
                    (BucketInitialTrustScoreEventsData) SerializationUtils.deserialize(
                            this.getByKey(BucketEvents.class.getName(), bucketDocumentScoreOld.getBytes()));

            InitialTrustScoreData initialTrustScoreData = bucketInitialTrustScoreEventsData.getInitialTrustTypeToInitialTrustScoreDataMap().get(InitialTrustScoreType.KYC);

            KYCDocumentScoreData kYCDocumentScoreData = new KYCDocumentScoreData();

            kYCDocumentScoreData.setEventDate(trustScoreDataCreateDate);
            String dateString = userTrustScoreData.getCreateTime().toString();
            Hash hash = new Hash(ByteBuffer.allocate(trustScoreData.getUserHash().getBytes().length + dateString.getBytes().length).
                    put(trustScoreData.getUserHash().getBytes()).put(dateString.getBytes()).array());
            kYCDocumentScoreData.setHash(hash);
            kYCDocumentScoreData.setSignerHash(trustScoreData.getSignerHash());
            kYCDocumentScoreData.setSignature(trustScoreData.getSignature());
            kYCDocumentScoreData.setScore(Math.max(trustScoreData.getKycTrustScore() - 10, 0));

            DocumentDecayedContributionData documentDecayedContributionData;

            TransactionUserParameters userParameters = userParametersAdHoc(userTrustScoreData.getUserType().toString());
            LocalDate lastUpdate = LocalDate.of(bucketInitialTrustScoreEventsData.getLastUpdate().getYear()+1900,
                    bucketInitialTrustScoreEventsData.getLastUpdate().getMonth()+1, bucketInitialTrustScoreEventsData.getLastUpdate().getDate());
            if (lastUpdate == null) { lastUpdate =  trustScoreDataCreateDate; }
            int dateDiff = (int) ChronoUnit.DAYS.between(trustScoreDataCreateDate, lastUpdate);
            if (initialTrustScoreData != null) {
                documentDecayedContributionData = new DocumentDecayedContributionData(
                        Math.max(initialTrustScoreData.getOriginalTrustScore(), 0),
                        DatesCalculation.calculateDecay(userParameters.getSemiDecay(), Math.max(initialTrustScoreData.getDecayedTrustScore(), 0), dateDiff));
            } else {
                documentDecayedContributionData = new DocumentDecayedContributionData(
                        Math.max(trustScoreData.getKycTrustScore() - 10, 0),
                        DatesCalculation.calculateDecay(userParameters.getSemiDecay(), Math.max(trustScoreData.getKycTrustScore() - 10, 0), dateDiff));
            }

            BucketDocumentScoreData bucketDocumentScoreData = new BucketDocumentScoreData();
            bucketDocumentScoreData.setHash(bucketDocumentScoreNew);
            bucketDocumentScoreData.setUserType(userTrustScoreData.getUserType());
            bucketDocumentScoreData.setLastUpdate(lastUpdate);
            bucketDocumentScoreData.getActualScoresDataMap().put(FinalScoreType.KYC, documentDecayedContributionData);
            bucketDocumentScoreData.getEventDataHashToEventDataMap().put(kYCDocumentScoreData.getHash(), kYCDocumentScoreData);

            Hash bucketTransactionsNew = getBucketHashByUserHashAndScoreType(userTrustScoreData.getHash(), ScoreType.TRANSACTION.value);
            Hash bucketTransactionsOld = trustScoreData.getEventTypeToBucketHashMap().get(EventType.TRANSACTION);

            BucketTransactionEventsData bucketTransactionEventsData =
                    (BucketTransactionEventsData) SerializationUtils.deserialize(
                            this.getByKey(BucketEvents.class.getName(), bucketTransactionsOld.getBytes()));

            BucketTransactionScoreData bucketTransactionScoreData = new BucketTransactionScoreData();

            bucketTransactionScoreData.setUserType(userTrustScoreData.getUserType());
            bucketTransactionScoreData.setHash(bucketTransactionsNew);
            // skip eventDataHashToEventDataMap;

            bucketTransactionScoreData.setCurrentDateNumberOfTransactions(bucketTransactionEventsData.getCurrentDateNumberOfTransactions());
            bucketTransactionScoreData.setCurrentDateNumberOfTransactionsContribution(bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution());
            bucketTransactionScoreData.setOldDateNumberOfTransactionsContribution(bucketTransactionEventsData.getOldDateNumberOfTransactionsContribution());

            bucketTransactionScoreData.setCurrentDateTurnOver(bucketTransactionEventsData.getCurrentDateTurnOver());
            bucketTransactionScoreData.setCurrentDateTurnOverContribution(bucketTransactionEventsData.getCurrentDateTurnOverContribution());
            bucketTransactionScoreData.setOldDateTurnOverContribution(bucketTransactionEventsData.getOldDateTurnOverContribution());

            LocalDate maxOldDate = null;
            double sumCurrentMonthBalanceContribution = 0;
            double sumOldMonthBalanceContribution = 0;
            Double lastOldCount = null;
            LocalDate todayDate = LocalDate.now(ZoneOffset.UTC);

            if (userParameters.getBalanceSemiDecay() != 0){  // so excluding nodes
                for (Map.Entry<Date, BalanceCountAndContribution> entry : bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution().entrySet()) {
                    LocalDate currentDate = LocalDate.of(entry.getKey().getYear()+1900, entry.getKey().getMonth()+1, entry.getKey().getDate());
                    int daysDiff = (int) ChronoUnit.DAYS.between(currentDate, todayDate);
                    double dailyScore = DatesCalculation.calculateDecay(userParameters.getBalanceSemiDecay(), Math.tanh(entry.getValue().getCount() /
                            userParameters.getBalanceLevel08() * UserParameters.atanh08), daysDiff);
                    if (daysDiff > 30) {
                        sumOldMonthBalanceContribution += dailyScore;
                        if (maxOldDate == null) {
                            maxOldDate = currentDate;
                            lastOldCount = entry.getValue().getCount();
                        }
                        else {
                            if (maxOldDate.isBefore(currentDate)) {maxOldDate = currentDate;}
                        }
                    }
                    else {
                        bucketTransactionScoreData.getCurrentMonthDayToBalanceCountAndContribution().put(
                                currentDate, new BalanceAndContribution(entry.getValue().getCount(), dailyScore));
                        sumCurrentMonthBalanceContribution += dailyScore;
                    }
                }

                if (lastOldCount != null || !bucketTransactionScoreData.getCurrentMonthDayToBalanceCountAndContribution().isEmpty()){
                    for (LocalDate currentDate = todayDate.minusDays(30); !currentDate.isAfter(todayDate); currentDate = currentDate.plusDays(1)){
                        if(bucketTransactionScoreData.getCurrentMonthDayToBalanceCountAndContribution().containsKey(currentDate)){
                            lastOldCount = bucketTransactionScoreData.getCurrentMonthDayToBalanceCountAndContribution().get(currentDate).getCount();
                        }
                        else if (lastOldCount != null) {
                            int daysDiff = (int) ChronoUnit.DAYS.between(currentDate, todayDate);
                            double dailyScore = DatesCalculation.calculateDecay(userParameters.getBalanceSemiDecay(), Math.tanh(lastOldCount /
                                    userParameters.getBalanceLevel08() * UserParameters.atanh08), daysDiff);
                            bucketTransactionScoreData.getCurrentMonthDayToBalanceCountAndContribution().put(
                                    currentDate, new BalanceAndContribution(lastOldCount, dailyScore));
                            sumCurrentMonthBalanceContribution += dailyScore;
                        }
                    }
                }
            }

            bucketTransactionScoreData.setCurrentMonthBalanceContribution(sumCurrentMonthBalanceContribution);
            bucketTransactionScoreData.setOldMonthBalanceContribution(sumOldMonthBalanceContribution);
            bucketTransactionScoreData.setLastUpdate(todayDate);

            createBucketsForUser(userTrustScoreData, bucketDocumentScoreData, bucketTransactionScoreData);

            this.put(UserTrustScores.class.getName(), userTrustScoreData.getHash().getBytes(), SerializationUtils.serialize(userTrustScoreData));

            iterator.next();
        }
        iterator.close();

    }

    //  Temporary section end
}
