package io.coti.trustscore.database;

import io.coti.basenode.data.Hash;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.exceptions.DataBaseException;
import io.coti.trustscore.data.Buckets.BucketInitialTrustScoreEventsData;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.BalanceCountAndContribution;
import io.coti.trustscore.data.Events.InitialTrustScoreData;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.data.UserTrustScoreData;
import io.coti.trustscore.data.contributiondata.BalanceAndContribution;
import io.coti.trustscore.data.contributiondata.DocumentDecayedContributionData;
import io.coti.trustscore.data.parameters.TransactionUserParameters;
import io.coti.trustscore.data.parameters.UserParameters;
import io.coti.trustscore.data.tsbuckets.BucketData;
import io.coti.trustscore.data.tsbuckets.BucketDocumentEventData;
import io.coti.trustscore.data.tsbuckets.BucketTransactionEventData;
import io.coti.trustscore.data.tsenums.EventType;
import io.coti.trustscore.data.tsenums.FinalEventType;
import io.coti.trustscore.data.tsevents.KYCDocumentEventData;
import io.coti.trustscore.model.*;
import io.coti.trustscore.utils.DatesCalculation;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
//        super.init(dbPath)

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
        } catch (DataBaseException e) {
            throw new DataBaseException("Error initiating Rocks DB. " + e.getMessage());
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

            }
        }
    }

    private void databaseDeleteColumnData(String name) {
        RocksIterator iterator;

        try {
            iterator = getIterator(name);
            iterator.seekToFirst();
            if (iterator.isValid()) {
                byte[] start = iterator.key();
                iterator.seekToLast();
                byte[] end = iterator.key();
                iterator.close();
                db.deleteRange(classNameToColumnFamilyHandleMapping.get(name), start, end);
            }
        } catch (RocksDBException e) {
            log.error(e.getMessage());
        }
    }

    private void databaseDropColumn(String name) {
        try {
            db.dropColumnFamily(classNameToColumnFamilyHandleMapping.get(name));
            classNameToColumnFamilyHandleMapping.get(name).close();
            classNameToColumnFamilyHandleMapping.remove(name);
            columnFamilyClassNames.remove(name);
        } catch (RocksDBException e) {
            log.error(e.getMessage());
        }
    }

//  Temporary section begin

    private void createBucketsForUser(UserTrustScoreData usertrustScoreData, BucketDocumentEventData bucketDocumentEventData, BucketTransactionEventData bucketTransactionEventData) {
        try {
            for (EventType score : EventType.values()) {

                BucketData bucketData;
                switch (score) {
                    case DOCUMENT_SCORE:
                        bucketData = bucketDocumentEventData;
                        break;
                    case TRANSACTION:
                        bucketData = bucketTransactionEventData;
                        break;
                    default:
                        bucketData = (BucketData) score.getBucket().getDeclaredConstructor().newInstance();
                        bucketData.setUserType(usertrustScoreData.getUserType());
                        bucketData.setHash(getBucketHashByUserHashAndScoreType(usertrustScoreData.getHash(), score.getValue()));
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
        Calendar calendar = new GregorianCalendar();

        iterator = getIterator(TrustScores.class.getName());
        iterator.seekToFirst();
        while (iterator.isValid()) {
            TrustScoreData trustScoreData = (TrustScoreData) SerializationUtils.deserialize(iterator.value());

            UserTrustScoreData userTrustScoreData = new UserTrustScoreData(trustScoreData.getUserHash(), trustScoreData.getUserType().toString());
            LocalDateTime trustScoreDataCreateTime = LocalDateTime.ofInstant(trustScoreData.getCreateTime().toInstant(), ZoneId.of("Asia/Jerusalem"));
            userTrustScoreData.setCreateTime(trustScoreDataCreateTime);

            calendar.setTime(trustScoreData.getCreateTime());
            LocalDate trustScoreDataCreateDate = LocalDate.of(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            userTrustScoreData.setZeroTrustFlag(trustScoreData.getZeroTrustFlag());

            Hash bucketDocumentScoreOld = trustScoreData.getEventTypeToBucketHashMap().get(io.coti.trustscore.data.Enums.EventType.INITIAL_EVENT);
            Hash bucketDocumentScoreNew = getBucketHashByUserHashAndScoreType(userTrustScoreData.getHash(), EventType.DOCUMENT_SCORE.getValue());

            BucketInitialTrustScoreEventsData bucketInitialTrustScoreEventsData =
                    (BucketInitialTrustScoreEventsData) SerializationUtils.deserialize(
                            this.getByKey(BucketEvents.class.getName(), bucketDocumentScoreOld.getBytes()));

            InitialTrustScoreData initialTrustScoreData = bucketInitialTrustScoreEventsData.getInitialTrustTypeToInitialTrustScoreDataMap().get(InitialTrustScoreType.KYC);

            KYCDocumentEventData kYCDocumentScoreData = new KYCDocumentEventData();

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

            calendar.setTime(bucketInitialTrustScoreEventsData.getLastUpdate());
            LocalDate lastUpdate = LocalDate.of(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            if (lastUpdate == null) {
                lastUpdate = trustScoreDataCreateDate;
            }
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

            BucketDocumentEventData bucketDocumentEventData = new BucketDocumentEventData();
            bucketDocumentEventData.setHash(bucketDocumentScoreNew);
            bucketDocumentEventData.setUserType(userTrustScoreData.getUserType());
            bucketDocumentEventData.setLastUpdate(lastUpdate);
            bucketDocumentEventData.getActualScoresDataMap().put(FinalEventType.KYC, documentDecayedContributionData);
            bucketDocumentEventData.getEventDataHashToEventDataMap().put(kYCDocumentScoreData.getHash(), kYCDocumentScoreData);

            Hash bucketTransactionsNew = getBucketHashByUserHashAndScoreType(userTrustScoreData.getHash(), EventType.TRANSACTION.getValue());
            Hash bucketTransactionsOld = trustScoreData.getEventTypeToBucketHashMap().get(io.coti.trustscore.data.Enums.EventType.TRANSACTION);

            BucketTransactionEventsData bucketTransactionEventsData =
                    (BucketTransactionEventsData) SerializationUtils.deserialize(
                            this.getByKey(BucketEvents.class.getName(), bucketTransactionsOld.getBytes()));

            BucketTransactionEventData bucketTransactionEventData = new BucketTransactionEventData();

            bucketTransactionEventData.setUserType(userTrustScoreData.getUserType());
            bucketTransactionEventData.setHash(bucketTransactionsNew);
            // skip eventDataHashToEventDataMap

            bucketTransactionEventData.setCurrentDateNumberOfTransactions(bucketTransactionEventsData.getCurrentDateNumberOfTransactions());
            bucketTransactionEventData.setCurrentDateNumberOfTransactionsContribution(bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution());
            bucketTransactionEventData.setOldDateNumberOfTransactionsContribution(bucketTransactionEventsData.getOldDateNumberOfTransactionsContribution());

            bucketTransactionEventData.setCurrentDateTurnOver(bucketTransactionEventsData.getCurrentDateTurnOver());
            bucketTransactionEventData.setCurrentDateTurnOverContribution(bucketTransactionEventsData.getCurrentDateTurnOverContribution());
            bucketTransactionEventData.setOldDateTurnOverContribution(bucketTransactionEventsData.getOldDateTurnOverContribution());

            LocalDate maxOldDate = null;
            double sumCurrentMonthBalanceContribution = 0;
            double sumOldMonthBalanceContribution = 0;
            BigDecimal lastOldCount = null;
            LocalDate todayDate = LocalDate.now(ZoneOffset.UTC);

            if (userParameters.getBalanceSemiDecay() != 0) {  // so excluding nodes
                for (Map.Entry<Date, BalanceCountAndContribution> entry : bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution().entrySet()) {

                    calendar.setTime(entry.getKey());
                    LocalDate currentDate = LocalDate.of(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                    int daysDiff = (int) ChronoUnit.DAYS.between(currentDate, todayDate);
                    double dailyScore = DatesCalculation.calculateDecay(userParameters.getBalanceSemiDecay(), Math.tanh(entry.getValue().getCount() /
                            userParameters.getBalanceLevel08() * UserParameters.ATANH08), daysDiff);
                    if (daysDiff > 30) {
                        sumOldMonthBalanceContribution += dailyScore;
                        if (maxOldDate == null) {
                            maxOldDate = currentDate;
                            lastOldCount = BigDecimal.valueOf(entry.getValue().getCount());
                        } else {
                            if (maxOldDate.isBefore(currentDate)) {
                                maxOldDate = currentDate;
                            }
                        }
                    } else {
                        bucketTransactionEventData.getCurrentMonthDayToBalanceCountAndContribution().put(
                                currentDate, new io.coti.trustscore.data.contributiondata.BalanceAndContribution(BigDecimal.valueOf(entry.getValue().getCount()), dailyScore));
                        sumCurrentMonthBalanceContribution += dailyScore;
                    }
                }

                if (lastOldCount != null || !bucketTransactionEventData.getCurrentMonthDayToBalanceCountAndContribution().isEmpty()) {
                    for (LocalDate currentDate = todayDate.minusDays(30); !currentDate.isAfter(todayDate); currentDate = currentDate.plusDays(1)) {
                        if (bucketTransactionEventData.getCurrentMonthDayToBalanceCountAndContribution().containsKey(currentDate)) {
                            lastOldCount = bucketTransactionEventData.getCurrentMonthDayToBalanceCountAndContribution().get(currentDate).getCount();
                        } else if (lastOldCount != null) {
                            int daysDiff = (int) ChronoUnit.DAYS.between(currentDate, todayDate);
                            double dailyScore = DatesCalculation.calculateDecay(userParameters.getBalanceSemiDecay(), Math.tanh(lastOldCount.doubleValue() /
                                    userParameters.getBalanceLevel08() * UserParameters.ATANH08), daysDiff);
                            bucketTransactionEventData.getCurrentMonthDayToBalanceCountAndContribution().put(
                                    currentDate, new BalanceAndContribution(lastOldCount, dailyScore));
                            sumCurrentMonthBalanceContribution += dailyScore;
                        }
                    }
                }
            }

            bucketTransactionEventData.setCurrentMonthBalanceContribution(sumCurrentMonthBalanceContribution);
            bucketTransactionEventData.setOldMonthBalanceContribution(sumOldMonthBalanceContribution);
            bucketTransactionEventData.setLastUpdate(todayDate);

            createBucketsForUser(userTrustScoreData, bucketDocumentEventData, bucketTransactionEventData);

            this.put(UserTrustScores.class.getName(), userTrustScoreData.getHash().getBytes(), SerializationUtils.serialize(userTrustScoreData));

            iterator.next();
        }
        iterator.close();

    }

    //  Temporary section end
}
