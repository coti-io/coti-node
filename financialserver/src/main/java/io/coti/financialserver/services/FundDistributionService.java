package io.coti.financialserver.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.financialserver.crypto.FundDistributionFileCrypto;
import io.coti.financialserver.crypto.FundDistributionFileResultCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.*;
import io.coti.financialserver.http.data.*;
import io.coti.financialserver.model.DailyFundDistributionFiles;
import io.coti.financialserver.model.DailyFundDistributions;
import io.coti.financialserver.model.FailedFundDistributions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class FundDistributionService {

    private static final int NUMBER_OF_DISTRIBUTION_LINE_DETAILS = 7;
    private static final int NUMBER_OF_DISTRIBUTION_SIGNATURE_LINE_DETAILS = 2;
    private static final String DAILY_DISTRIBUTION_RESULT_FILE_PREFIX = "distribution_results_";
    private static final String DAILY_DISTRIBUTION_RESULT_FILE_SUFFIX = ".csv";
    private static final String COMMA_SEPARATOR = ",";

    @Value("${financialserver.seed}")
    private String seed;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;
    @Value("${distribution.cron.enabled}")
    private boolean distributionCronEnabled;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private BaseNodeBalanceService baseNodeBalanceService;
    @Autowired
    protected INetworkService networkService;
    @Autowired
    private AwsService awsService;
    @Autowired
    private FundDistributionFileCrypto fundDistributionFileCrypto;
    @Autowired
    private FundDistributionFileResultCrypto fundDistributionFileResultCrypto;
    @Autowired
    private DailyFundDistributions dailyFundDistributions;
    @Autowired
    private FailedFundDistributions failedFundDistributions;
    @Autowired
    private DailyFundDistributionFiles dailyFundDistributionFiles;
    private Map<Hash, FundDistributionReservedBalanceData> fundReservedBalanceMap;
    private Map<Hash, ReservedBalanceData> addressToReservedBalanceMap;

    public void initReservedBalance() {
        fundReservedBalanceMap = new ConcurrentHashMap<>();
        addressToReservedBalanceMap = new ConcurrentHashMap<>();
        for (Fund fund : Fund.values()) {
            Hash fundAddress = (fund.getFundHash() == null) ? getFundAddressHash(fund) : fund.getFundHash();
            FundDistributionReservedBalanceData fundDistributionReservedBalanceData = new FundDistributionReservedBalanceData(fund, BigDecimal.ZERO);
            fundReservedBalanceMap.put(fundAddress, fundDistributionReservedBalanceData);
        }
        insertReservedAmountsFromPendingTransactions();
    }

    private void insertReservedAmountsFromPendingTransactions() {
        dailyFundDistributions.forEach(dailyFundDistributionData ->
                dailyFundDistributionData.getFundDistributionEntries().values().forEach(fundDistributionData -> {
                    if (fundDistributionData.isLockingAmount()) {
                        Hash fundAddress = (fundDistributionData.getDistributionPoolFund().getFundHash() == null) ?
                                getFundAddressHash(fundDistributionData.getDistributionPoolFund()) :
                                fundDistributionData.getDistributionPoolFund().getFundHash();
                        FundDistributionReservedBalanceData fundDistributionReservedBalanceData = fundReservedBalanceMap.get(fundAddress);
                        BigDecimal updatedFundLockedAmount = fundDistributionReservedBalanceData.getReservedAmount().add(fundDistributionData.getAmount());
                        fundDistributionReservedBalanceData.setReservedAmount(updatedFundLockedAmount);

                        updateAddressToReservedBalanceMap(fundDistributionData.getReceiverAddress(), fundDistributionData.getAmount());
                    }
                })
        );
    }

    private void updateAddressToReservedBalanceMap(Hash receiverAddress, BigDecimal distributionAmount) {
        ReservedBalanceData reservedBalanceData = addressToReservedBalanceMap.get(receiverAddress);
        if (reservedBalanceData == null) {
            reservedBalanceData = new ReservedBalanceData(BigDecimal.ZERO);
            addressToReservedBalanceMap.put(receiverAddress, reservedBalanceData);
        }
        reservedBalanceData.setReservedAmount(reservedBalanceData.getReservedAmount().add(distributionAmount));
    }

    private Hash getFundAddressHash(Fund fund) {
        Hash fundAddress = fund.getFundHash();
        if (fundAddress == null) {
            fundAddress = nodeCryptoHelper.generateAddress(seed, Math.toIntExact(fund.getReservedAddress().getIndex()));
            fund.setFundHash(fundAddress);
        }
        return fundAddress;
    }

    public ResponseEntity<IResponse> getFundBalances() {
        List<FundDistributionBalanceResultData> fundDistributionBalanceResultDataList = new ArrayList<>();
        fundReservedBalanceMap.values().forEach(fundDistributionReservedBalanceData -> {
            Hash fundAddress = (fundDistributionReservedBalanceData.getFund().getFundHash() == null) ?
                    getFundAddressHash(fundDistributionReservedBalanceData.getFund()) : fundDistributionReservedBalanceData.getFund().getFundHash();
            fundDistributionBalanceResultDataList.add(
                    new FundDistributionBalanceResultData(fundDistributionReservedBalanceData.getFund().getText(),
                            baseNodeBalanceService.getBalanceByAddress(fundAddress),
                            baseNodeBalanceService.getPreBalanceByAddress(fundAddress),
                            fundDistributionReservedBalanceData.getReservedAmount()));
        });
        return ResponseEntity.status(HttpStatus.OK)
                .body(new FundDistributionBalanceResponse(new FundDistributionBalanceResponseData(fundDistributionBalanceResultDataList)));
    }

    public ResponseEntity<IResponse> getReservedBalances(GetReservedBalancesRequest getReservedBalancesRequest) {
        Set<ReservedBalanceResponseData> reservedBalances = new HashSet<>();
        getReservedBalancesRequest.getAddresses().forEach(address -> {
            if (addressToReservedBalanceMap.containsKey(address)) {
                reservedBalances.add(new ReservedBalanceResponseData(address, addressToReservedBalanceMap.get(address).getReservedAmount()));
            }
        });
        return ResponseEntity.status(HttpStatus.OK).body(new GetReservedBalancesResponse(reservedBalances));
    }

    public ResponseEntity<IResponse> distributeFundFromLocalFile(AddFundDistributionsRequest request) {
        List<FundDistributionData> fundDistributionFileDataEntries = new ArrayList<>();
        ResponseEntity<IResponse> distributionFileVerificationResponse = verifyDailyDistributionLocalFile(request, fundDistributionFileDataEntries);

        if (distributionFileVerificationResponse != null) {
            return distributionFileVerificationResponse;
        }

        ResponseEntity<IResponse> responseEntity = updateWithTransactionsEntriesFromVerifiedFile(fundDistributionFileDataEntries, new AtomicLong(0), new AtomicLong(0));

        return responseEntity;

    }

    public ResponseEntity<IResponse> distributeFundFromFile(AddFundDistributionsRequest request) {
        AtomicLong acceptedDistributionNumber = new AtomicLong(0);
        AtomicLong notAcceptedDistributionNumber = new AtomicLong(0);
        Thread monitorDistributionFile = monitorDistributionFile(acceptedDistributionNumber, notAcceptedDistributionNumber);
        try {
            Instant now = Instant.now();
            Hash hashOfToday = getHashOfDate(now);


            DailyFundDistributionFileData fundDistributionFileByDayByHash = dailyFundDistributionFiles.getByHash(hashOfToday);
            if (fundDistributionFileByDayByHash != null) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new Response(DISTRIBUTION_FILE_ALREADY_PROCESSED, STATUS_ERROR));
            }

            List<FundDistributionData> fundDistributionFileDataEntries = new ArrayList<>();
            ResponseEntity<IResponse> distributionFileVerificationResponse = verifyDailyDistributionFile(request, fundDistributionFileDataEntries);
            if (distributionFileVerificationResponse != null) {
                return distributionFileVerificationResponse;
            }
            monitorDistributionFile.start();
            ResponseEntity<IResponse> responseEntity = updateWithTransactionsEntriesFromVerifiedFile(fundDistributionFileDataEntries, acceptedDistributionNumber, notAcceptedDistributionNumber);

            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                String fileName = request.getFileName();
                DailyFundDistributionFileData fundDistributionFileOfDay = new DailyFundDistributionFileData(now, fileName);
                dailyFundDistributionFiles.put(fundDistributionFileOfDay);
            }
            return responseEntity;
        } finally {
            if (monitorDistributionFile.isAlive()) {
                monitorDistributionFile.interrupt();
            }
        }


    }

    private Thread monitorDistributionFile(AtomicLong acceptedDistributionNumber, AtomicLong notAcceptedDistributionNumber) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("Fund distributions, accepted : {}, rejected: {}", acceptedDistributionNumber, notAcceptedDistributionNumber);
            }
        });
    }

    public LocalDateTime getStartOfYesterday() {
        return LocalDate.now(ZoneId.of("UTC")).minusDays(1).atStartOfDay();
    }

    private ResponseEntity<IResponse> verifyDailyDistributionFile(AddFundDistributionsRequest request, List<FundDistributionData> fundDistributionFileDataEntries) {
        FundDistributionFileData fundDistributionFileData = request.getFundDistributionFileData(new Hash(kycServerPublicKey));
        String fileName = request.getFileName();

        ResponseEntity<IResponse> response = verifyDailyDistributionFileByName(fundDistributionFileDataEntries, fundDistributionFileData, fileName);
        if (response != null) {
            return response;
        }

        return null;
    }

    private ResponseEntity<IResponse> verifyDailyDistributionLocalFile(AddFundDistributionsRequest request, List<FundDistributionData> fundDistributionFileDataEntries) {
        FundDistributionFileData fundDistributionFileData = request.getFundDistributionFileData(new Hash(kycServerPublicKey));
        String fileName = request.getFileName();

        ResponseEntity<IResponse> response = verifyDailyDistributionLocalFileByName(fundDistributionFileDataEntries, fundDistributionFileData, fileName);
        if (response != null) {
            return response;
        }

        return null;
    }

    private ResponseEntity<IResponse> verifyDailyDistributionLocalFileByName(List<FundDistributionData> fundDistributionFileDataEntries, FundDistributionFileData fundDistributionFileData, String fileName) {
        ResponseEntity<IResponse> responseEntityForFileHandling = handleFundDistributionFile(fundDistributionFileData, fileName, fundDistributionFileDataEntries);
        if (responseEntityForFileHandling != null) {
            return responseEntityForFileHandling;
        }
        if (fundDistributionFileData.getUserSignature() == null || !fundDistributionFileCrypto.verifySignature(fundDistributionFileData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        return null;
    }

    private ResponseEntity<IResponse> verifyDailyDistributionFileByName(List<FundDistributionData> fundDistributionFileDataEntries, FundDistributionFileData fundDistributionFileData, String fileName) {
        try {
            awsService.downloadFundDistributionFile(fileName);
        } catch (IOException e) {
            log.error(CANT_SAVE_FILE_ON_DISK, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(CANT_SAVE_FILE_ON_DISK, STATUS_ERROR));
        }

        ResponseEntity<IResponse> responseEntityForFileHandling = handleFundDistributionFile(fundDistributionFileData, fileName, fundDistributionFileDataEntries);
        if (responseEntityForFileHandling != null) {
            return responseEntityForFileHandling;
        }
        if (fundDistributionFileData.getUserSignature() == null || !fundDistributionFileCrypto.verifySignature(fundDistributionFileData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        return null;
    }

    public ResponseEntity<IResponse> handleFundDistributionFile(FundDistributionFileData fundDistributionFileData, String fileName, List<FundDistributionData> fundDistributionEntries) {
        String line = "";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    break;
                String[] distributionDetails;
                distributionDetails = line.split(COMMA_SEPARATOR);
                if (distributionDetails.length != NUMBER_OF_DISTRIBUTION_LINE_DETAILS) {
                    if (distributionDetails.length == NUMBER_OF_DISTRIBUTION_SIGNATURE_LINE_DETAILS) {
                        String rHex = distributionDetails[FundDistributionEntry.SIGNATURE_R.getIndex()];
                        String sHex = distributionDetails[FundDistributionEntry.SIGNATURE_S.getIndex()];
                        fundDistributionFileData.setSignature(new SignatureData(rHex, sHex));
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(line, PARSED_WITH_ERROR));
                    }
                } else {
                    FundDistributionData entryData = handleFundDistributionFileLine(fundDistributionFileData, distributionDetails, fileName);
                    if (entryData != null) {
                        fundDistributionEntries.add(entryData);
                    } else {
                        log.error(BAD_CSV_FILE_LINE_FORMAT);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(line, BAD_CSV_FILE_LINE_FORMAT));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Errors on distribution funds service: {}", e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new Response(line, BAD_CSV_FILE_LINE_FORMAT));
        }
        return null;
    }

    private FundDistributionData handleFundDistributionFileLine(FundDistributionFileData fundDistributionFileData, String[] distributionDetails, String fileName) {
        try {
            long id = Long.parseLong(distributionDetails[FundDistributionEntry.ID.getIndex()]);
            Hash receiverAddress = new Hash(distributionDetails[FundDistributionEntry.RECEIVER_ADDRESS.getIndex()]);
            String distributionPool = distributionDetails[FundDistributionEntry.DISTRIBUTION_POOL.getIndex()];
            BigDecimal amount = new BigDecimal(distributionDetails[FundDistributionEntry.AMOUNT.getIndex()]);
            Instant createTime = Instant.parse(distributionDetails[FundDistributionEntry.CREATION_TIME.getIndex()]);
            Instant transactionTime = Instant.parse(distributionDetails[FundDistributionEntry.RELEASE_TIME.getIndex()]);
            String source = distributionDetails[FundDistributionEntry.SOURCE.getIndex()];

            FundDistributionData entryData =
                    new FundDistributionData(id, receiverAddress, Fund.getFundByText(distributionPool),
                            amount, createTime, transactionTime, source);

            byte[] receiverAddressInBytes = receiverAddress.getBytes();
            byte[] distributionPoolInBytes = distributionPool.getBytes();
            byte[] amountInBytes = amount.stripTrailingZeros().toPlainString().getBytes();
            byte[] sourceInBytes = source.getBytes();

            byte[] entryDataInBytes = ByteBuffer.allocate(Long.BYTES + receiverAddressInBytes.length + distributionPoolInBytes.length
                    + amountInBytes.length + Long.BYTES + Long.BYTES + sourceInBytes.length)
                    .putLong(id).put(receiverAddressInBytes).put(distributionPoolInBytes).put(amountInBytes)
                    .putLong(createTime.toEpochMilli()).putLong(transactionTime.toEpochMilli()).put(sourceInBytes)
                    .array();
            fundDistributionFileData.getSignatureMessage().add(entryDataInBytes);
            fundDistributionFileData.incrementMessageByteSize(entryDataInBytes.length);

            entryData.setFileName(fileName);
            return entryData;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private ResponseEntity<IResponse> updateWithTransactionsEntriesFromVerifiedFile(List<FundDistributionData> fundDistributionFileEntriesData, AtomicLong acceptedDistributionNumber, AtomicLong notAcceptedDistributionNumber) {
        List<FundDistributionFileEntryResultData> fundDistributionFileEntryResults = new ArrayList<>();
        fundDistributionFileEntriesData.forEach(entryData -> {
                    boolean isAddressValid;
                    boolean isLockupDateValid = false;
                    boolean uniqueByDate = false;
                    boolean passedPreBalanceCheck = false;
                    boolean accepted = (isAddressValid = isAddressValid(entryData)) && (isLockupDateValid = isLockupDateValid(entryData)) && (uniqueByDate = isEntryDataUniquePerDate(entryData)) &&
                            (passedPreBalanceCheck = updateFundAvailableLockedBalances(entryData));

                    if (accepted) {
                        acceptedDistributionNumber.incrementAndGet();
                        entryData.setStatus(DistributionEntryStatus.ACCEPTED);
                        DailyFundDistributionData fundDistributionOfDay = dailyFundDistributions.getByHash(entryData.getHashByDate());
                        if (fundDistributionOfDay == null) {
                            LinkedHashMap<Hash, FundDistributionData> fundDistributionEntries = new LinkedHashMap<>();
                            fundDistributionOfDay = new DailyFundDistributionData(entryData.getReleaseTime(), fundDistributionEntries);
                        }
                        fundDistributionOfDay.getFundDistributionEntries().put(entryData.getHash(), entryData);
                        dailyFundDistributions.put(fundDistributionOfDay);
                    } else {
                        notAcceptedDistributionNumber.incrementAndGet();
                    }
                    String statusByChecks = getTransactionEntryStatusByChecks(isAddressValid, isLockupDateValid, uniqueByDate, passedPreBalanceCheck);
                    fundDistributionFileEntryResults.add(new FundDistributionFileEntryResultData(entryData.getId(), entryData.getReceiverAddress().toString(),
                            entryData.getDistributionPoolFund().getText(), entryData.getSource(), accepted, statusByChecks));
                }
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(new AddFundDistributionsResponse(new AddFundDistributionsResponseData(fundDistributionFileEntryResults)));
    }

    private String getTransactionEntryStatusByChecks(boolean isAddressValid, boolean isLockupDateValid, boolean uniqueByDate, boolean passedPreBalanceCheck) {
        if (!isAddressValid) {
            return RECEIVER_ADDRESS_INVALID;
        }
        if (!isLockupDateValid) {
            return LOCK_UP_DATE_IS_INVALID;
        }
        if (!uniqueByDate) {
            return DATE_UNIQUENESS_WAS_NOT_MAINTAINED;
        }
        if (!passedPreBalanceCheck) {
            return DISTRIBUTION_POOL_BALANCE_CHECKS_FAILED;
        }
        return ACCEPTED;
    }

    private boolean updateFundAvailableLockedBalances(FundDistributionData entryData) {
        return updateFundAvailableLockedBalances(entryData.getDistributionPoolFund().getFundHash(), entryData.getReceiverAddress(), entryData.getAmount(), false);
    }

    private boolean updateFundAvailableLockedBalances(Hash fundAddress, Hash receiverAddress, BigDecimal amount, boolean allowNegative) {
        if (!allowNegative && amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        FundDistributionReservedBalanceData fundDistributionReservedBalanceData = fundReservedBalanceMap.get(fundAddress);
        BigDecimal updatedAmountToLock = fundDistributionReservedBalanceData.getReservedAmount().add(amount);
        if (updatedAmountToLock.compareTo(baseNodeBalanceService.getPreBalanceByAddress(fundAddress)) > 0 ||
                updatedAmountToLock.compareTo(baseNodeBalanceService.getBalanceByAddress(fundAddress)) > 0) {
            return false;
        } else {
            fundDistributionReservedBalanceData.setReservedAmount(updatedAmountToLock);
            updateAddressToReservedBalanceMap(receiverAddress, amount);
        }
        return true;
    }

    private boolean isEntryDataUniquePerDate(FundDistributionData entryData) {
        Instant transactionReleaseDate = entryData.getReleaseTime();
        Hash hashOfDate = getHashOfDate(transactionReleaseDate);
        return dailyFundDistributions.getByHash(hashOfDate) == null ||
                dailyFundDistributions.getByHash(hashOfDate).getFundDistributionEntries().get(entryData.getHash()) == null;
    }

    private boolean isAddressValid(FundDistributionData entryData) {
        return CryptoHelper.isAddressValid(entryData.getReceiverAddress());
    }

    private boolean isLockupDateValid(FundDistributionData entryData) {
        Instant transactionReleaseDate = entryData.getReleaseTime();
        return transactionReleaseDate != null && !LocalDateTime.ofInstant(transactionReleaseDate, ZoneOffset.UTC).isBefore(getStartOfYesterday());
    }


    @Scheduled(cron = "${distribution.cron.time}", zone = "UTC")
    public void scheduleTaskUsingCronExpression() {
        if (distributionCronEnabled) {
            log.info("Starting scheduled action for creating distribution transactions");
            AtomicLong createdTransactionNumber = new AtomicLong(0);
            AtomicLong failedTransactionNumber = new AtomicLong(0);
            Thread monitorCreatedTransactions = monitorCreatedTransactions(createdTransactionNumber, failedTransactionNumber);
            try {
                monitorCreatedTransactions.start();
                createPendingTransactions(createdTransactionNumber, failedTransactionNumber);
            } catch (Exception e) {
                log.error("Error at distribution transactions : {}", e.getMessage());
            } finally {
                monitorCreatedTransactions.interrupt();
            }
            log.info("Finished scheduled action for creating distribution transactions");
        }
    }

    private Thread monitorCreatedTransactions(AtomicLong createdTransactionNumber, AtomicLong failedTransactionNumber) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("Created transactions: {}, failed transactions: {}", createdTransactionNumber, failedTransactionNumber);
            }
        });
    }

    private void createPendingTransactions(AtomicLong createdTransactionNumber, AtomicLong failedTransactionNumber) {
        List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList = new ArrayList<>();
        try {
            createPendingFailedTransactions(fundDistributionFileEntryResultDataList, createdTransactionNumber, failedTransactionNumber);
            createPendingNonFailedTransactionsByDate(fundDistributionFileEntryResultDataList, createdTransactionNumber, failedTransactionNumber);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        createFundDistributionFileResult(fundDistributionFileEntryResultDataList);
    }

    private void createPendingNonFailedTransactionsByDate(List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList, AtomicLong createdTransactionNumber, AtomicLong failedTransactionNumber) throws InterruptedException {
        Hash hashOfYesterday = getHashOfDate(getStartOfYesterday());
        if (dailyFundDistributions.getByHash(hashOfYesterday) == null) {
            return;
        }
        DailyFundDistributionData dailyFundDistributionData = dailyFundDistributions.getByHash(hashOfYesterday);
        LinkedHashMap<Hash, FundDistributionData> fundDistributionEntries = dailyFundDistributionData.getFundDistributionEntries();
        for (FundDistributionData fundDistributionData : fundDistributionEntries.values()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Pending non failed transaction thread is interrupted");
            }
            boolean isSuccessful = false;
            Hash initialTransactionHash;
            if (fundDistributionData.isReadyToInitiate()) {
                initialTransactionHash = createInitialTransactionToDistributionEntry(fundDistributionData);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (initialTransactionHash != null) {
                    createdTransactionNumber.incrementAndGet();
                    isSuccessful = true;
                    fundDistributionData.setStatus(DistributionEntryStatus.CREATED);
                    fundDistributionEntries.put(fundDistributionData.getHash(), fundDistributionData);
                    substractDistributionFromReservedBalanceMaps(fundDistributionData);
                } else {
                    failedTransactionNumber.incrementAndGet();
                    fundDistributionData.setStatus(DistributionEntryStatus.FAILED);
                    fundDistributionEntries.put(fundDistributionData.getHash(), fundDistributionData);
                    FailedFundDistributionData failedFundDistributionData = failedFundDistributions.getByHash(hashOfYesterday);
                    if (failedFundDistributionData == null) {
                        failedFundDistributionData = new FailedFundDistributionData(hashOfYesterday);
                    }
                    failedFundDistributionData.getFundDistributionHashes().put(fundDistributionData.getHash(), fundDistributionData.getHash());
                    failedFundDistributions.put(failedFundDistributionData);
                }
                dailyFundDistributions.put(dailyFundDistributionData);
                String status = isSuccessful ? TRANSACTION_CREATED_SUCCESSFULLY : TRANSACTION_CREATION_FAILED;
                FundDistributionFileEntryResultData fundDistributionFileEntryResultData = new FundDistributionFileEntryResultData(fundDistributionData.getId(),
                        fundDistributionData.getReceiverAddress().toString(), fundDistributionData.getDistributionPoolFund().getText(),
                        fundDistributionData.getSource(), isSuccessful, status);
                if (initialTransactionHash != null) {
                    fundDistributionFileEntryResultData.setTransactionHash(initialTransactionHash.toString());
                }
                fundDistributionFileEntryResultDataList.add(fundDistributionFileEntryResultData);

            }
        }
    }

    private void substractDistributionFromReservedBalanceMaps(FundDistributionData fundDistributionData) {

        FundDistributionReservedBalanceData fundReserveBalanceData = fundReservedBalanceMap.get(fundDistributionData.getDistributionPoolFund().getFundHash());
        BigDecimal updatedFundReservedAmount = fundReserveBalanceData.getReservedAmount().subtract(fundDistributionData.getAmount());
        if (updatedFundReservedAmount.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Fund reserved amount can not be negative.");
            updatedFundReservedAmount = BigDecimal.ZERO;
        }
        fundReserveBalanceData.setReservedAmount(updatedFundReservedAmount);

        Hash receiverAddress = fundDistributionData.getReceiverAddress();
        ReservedBalanceData reservedBalanceData = addressToReservedBalanceMap.get(receiverAddress);
        if (reservedBalanceData == null) {
            log.error("Receiver reserved balance doesn't exist");
            return;
        }
        BigDecimal updatedReservedAmount = reservedBalanceData.getReservedAmount().subtract(fundDistributionData.getAmount());
        if (updatedReservedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            addressToReservedBalanceMap.remove(receiverAddress);
            return;
        }
        reservedBalanceData.setReservedAmount(updatedReservedAmount);
    }

    private Hash getHashOfDate(Instant dayInstant) {
        return getHashOfDate(LocalDateTime.ofInstant(dayInstant, ZoneOffset.UTC));
    }

    private Hash getHashOfDate(LocalDateTime localDateTime) {
        return CryptoHelper.cryptoHash((localDateTime.getYear() + localDateTime.getMonth().toString() +
                localDateTime.getDayOfMonth()).getBytes());
    }

    private void createPendingFailedTransactions(List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList, AtomicLong createdTransactionNumber, AtomicLong failedTransactionNumber) {
        failedFundDistributions.forEach(failedFundDistributionData ->
        {
            Hash hashOfDay = failedFundDistributionData.getHash();
            DailyFundDistributionData dailyFundDistributionData = dailyFundDistributions.getByHash(hashOfDay);
            Iterator<Hash> failedEntryHashKeys = failedFundDistributionData.getFundDistributionHashes().keySet().iterator();
            while (failedEntryHashKeys.hasNext()) {
                Hash failedFundDistributionHash = failedEntryHashKeys.next();
                Hash initialTransactionHash;
                boolean isSuccessful = false;
                FundDistributionData fundDistributionData = dailyFundDistributionData.getFundDistributionEntries().get(failedFundDistributionHash);
                if (fundDistributionData.getStatus().equals(DistributionEntryStatus.FAILED)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.info("Pending failed transaction creation interrupted: {}", e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                    initialTransactionHash = createInitialTransactionToDistributionEntry(fundDistributionData);
                    if (initialTransactionHash != null) {
                        createdTransactionNumber.incrementAndGet();
                        isSuccessful = true;
                        fundDistributionData.setStatus(DistributionEntryStatus.CREATED);
                        failedEntryHashKeys.remove();
                        substractDistributionFromReservedBalanceMaps(fundDistributionData);
                    } else {
                        failedTransactionNumber.incrementAndGet();
                    }
                    String status = isSuccessful ? TRANSACTION_CREATED_SUCCESSFULLY : TRANSACTION_CREATION_FAILED;
                    FundDistributionFileEntryResultData fundDistributionFileEntryResultData = new FundDistributionFileEntryResultData(fundDistributionData.getId(), fundDistributionData.getReceiverAddress().toString(),
                            fundDistributionData.getDistributionPoolFund().getText(), fundDistributionData.getSource(), isSuccessful, status);
                    if (initialTransactionHash != null) {
                        fundDistributionFileEntryResultData.setTransactionHash(initialTransactionHash.toString());
                    }
                    fundDistributionFileEntryResultDataList.add(fundDistributionFileEntryResultData);
                } else {
                    failedEntryHashKeys.remove();
                }
            }
            dailyFundDistributions.put(dailyFundDistributionData);
            failedFundDistributions.put(failedFundDistributionData);
        });
    }

    private String createDistributionResultFileNameForToday() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        String today = ldt.getYear() + "-" + StringUtils.leftPad(Integer.toString(ldt.getMonthValue()), 2, "0") + "-" + StringUtils.leftPad(Integer.toString(ldt.getDayOfMonth()), 2, "0");
        return DAILY_DISTRIBUTION_RESULT_FILE_PREFIX + today + DAILY_DISTRIBUTION_RESULT_FILE_SUFFIX;
    }

    private Hash getEntryResultSourceFundAddress(FundDistributionFileEntryResultData entryResult) {
        int sourceAddressIndex = Math.toIntExact(Fund.getFundByText(entryResult.getDistributionPool()).getReservedAddress().getIndex());
        return nodeCryptoHelper.generateAddress(seed, sourceAddressIndex);
    }

    private Hash createInitialTransactionToDistributionEntry(FundDistributionData fundDistributionData) {
        Hash initialTransactionHash = null;
        try {
            int sourceAddressIndex = Math.toIntExact(fundDistributionData.getDistributionPoolFund().getReservedAddress().getIndex());
            Hash sourceAddress = fundDistributionData.getDistributionPoolFund().getFundHash();
            initialTransactionHash = transactionCreationService.createInitialTransactionToFund(fundDistributionData.getAmount(),
                    sourceAddress, fundDistributionData.getReceiverAddress(), sourceAddressIndex);
        } catch (Exception e) {
            log.error("Failed to create initial transaction.");
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }
        return initialTransactionHash;
    }

    private void createFundDistributionFileResult(List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList) {
        String resultsFileNameForToday = createDistributionResultFileNameForToday();
        File file = new File(resultsFileNameForToday);
        FundDistributionFileResultData fundDistributionFileResultData = new FundDistributionFileResultData();
        try (Writer fileWriter = new FileWriter(resultsFileNameForToday, false)) {
            for (FundDistributionFileEntryResultData entryResult : fundDistributionFileEntryResultDataList) {
                fileWriter.write(getEntryResultAsCommaDelimitedLine(entryResult));
                updateFundDistributionFileResultData(fundDistributionFileResultData, entryResult);
            }
            fundDistributionFileResultData.setFinancialServerHash(NodeCryptoHelper.getNodeHash());
            fundDistributionFileResultCrypto.signMessage(fundDistributionFileResultData);
            SignatureData signature = fundDistributionFileResultCrypto.getSignature(fundDistributionFileResultData);
            fileWriter.write(signature.getR() + COMMA_SEPARATOR + signature.getS()
                    + COMMA_SEPARATOR + fundDistributionFileResultData.getFinancialServerHash());
        } catch (IOException e) {
            log.error(CANT_SAVE_FILE_ON_DISK, e);
            return;
        }

        awsService.uploadFundDistributionResultFile(resultsFileNameForToday, file, "application/vnd.ms-excel");
    }

    private String getEntryResultAsCommaDelimitedLine(FundDistributionFileEntryResultData entryResult) {
        return Long.toString(entryResult.getId()) + COMMA_SEPARATOR + entryResult.getDistributionPool() + COMMA_SEPARATOR +
                entryResult.getSource() + COMMA_SEPARATOR + getEntryResultSourceFundAddress(entryResult).toString() + COMMA_SEPARATOR +
                entryResult.getReceiverAddress() + COMMA_SEPARATOR + ((Boolean) entryResult.isAccepted()).toString() + COMMA_SEPARATOR +
                entryResult.getStatus() + COMMA_SEPARATOR + entryResult.getTransactionHash() + "\n";
    }

    private void updateFundDistributionFileResultData(FundDistributionFileResultData fundDistributionFileResultData, FundDistributionFileEntryResultData entryResult) {
        byte[] distributionPoolNameInBytes = entryResult.getDistributionPool().getBytes();
        byte[] sourceInBytes = entryResult.getSource().getBytes();
        byte[] distributionPoolAddressInBytes = entryResult.getReceiverAddress().getBytes();
        byte[] receiverAddressInBytes = new Hash(entryResult.getReceiverAddress()).getBytes();
        byte[] isAcceptedInBytes = ((Boolean) entryResult.isAccepted()).toString().getBytes();
        byte[] statusInBytes = entryResult.getStatus().getBytes();
        byte[] transactionHashInBytes = entryResult.getTransactionHash() != null ? new Hash(entryResult.getTransactionHash()).getBytes() : new byte[0];

        byte[] resultLineInBytes = ByteBuffer.allocate(Long.BYTES + distributionPoolNameInBytes.length + sourceInBytes.length + distributionPoolAddressInBytes.length +
                receiverAddressInBytes.length + isAcceptedInBytes.length + statusInBytes.length + transactionHashInBytes.length).
                putLong(entryResult.getId()).put(distributionPoolNameInBytes).put(sourceInBytes).put(distributionPoolAddressInBytes).put(receiverAddressInBytes).
                put(statusInBytes).put(transactionHashInBytes).array();

        fundDistributionFileResultData.getSignatureMessage().add(resultLineInBytes);
        fundDistributionFileResultData.incrementMessageByteSize(resultLineInBytes.length);
    }

    public ResponseEntity<IResponse> deleteFundFileRecord() {
        Hash fundDistributionFileRecordHash = getHashOfDate(Instant.now());

        DailyFundDistributionFileData fundDistributionFileRecord = dailyFundDistributionFiles.getByHash(fundDistributionFileRecordHash);
        if (fundDistributionFileRecord != null) {
            dailyFundDistributionFiles.delete(fundDistributionFileRecord);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(DISTRIBUTION_FILE_RECORD_DELETED));
    }

    public ResponseEntity<IResponse> getFailedDistributions() {
        List<FundDistributionResponseData> fundDistributions = new ArrayList<>();

        failedFundDistributions.forEach(failedFundDistributionData ->
        {
            Hash hashOfDay = failedFundDistributionData.getHash();
            DailyFundDistributionData dailyFundDistributionData = dailyFundDistributions.getByHash(hashOfDay);
            failedFundDistributionData.getFundDistributionHashes().keySet().forEach(fundDistributionHash -> {
                FundDistributionData fundDistributionData = dailyFundDistributionData.getFundDistributionEntries().get(fundDistributionHash);
                fundDistributions.add(new FundDistributionResponseData(fundDistributionData));
            });

        });

        return ResponseEntity.ok().body(new GetFundDistributionsResponse(fundDistributions));
    }

    public ResponseEntity<IResponse> getDistributionsByDate(GetDistributionsByDateRequest getDistributionsByDateRequest) {
        List<FundDistributionResponseData> fundDistributions = new ArrayList<>();

        LocalDateTime distributionDate = getDistributionsByDateRequest.getDistributionDate();
        Hash hashOfDistributionDate = getHashOfDate(distributionDate);

        DailyFundDistributionData dailyFundDistributionData = dailyFundDistributions.getByHash(hashOfDistributionDate);

        if (dailyFundDistributionData != null) {
            LinkedHashMap<Hash, FundDistributionData> fundDistributionEntries = dailyFundDistributionData.getFundDistributionEntries();
            Hash distributionHash = getDistributionsByDateRequest.getDistributionHash();
            if (distributionHash.getBytes().length != 0) {
                FundDistributionData fundDistributionData = fundDistributionEntries.get(distributionHash);
                if (fundDistributionData != null) {
                    fundDistributions.add(new FundDistributionResponseData(fundDistributionData));
                }
            } else {
                fundDistributionEntries.values().forEach(fundDistributionData -> fundDistributions.add(new FundDistributionResponseData(fundDistributionData)));
            }
        }

        return ResponseEntity.ok().body(new GetFundDistributionsResponse(fundDistributions));
    }

    public ResponseEntity<IResponse> updateFundDistributionAmount(UpdateDistributionAmountRequest updateDistributionAmountRequest) {
        LocalDateTime distributionDate = updateDistributionAmountRequest.getDistributionDate();
        Hash hashOfDistributionDate = getHashOfDate(distributionDate);

        DailyFundDistributionData dailyFundDistributionData = dailyFundDistributions.getByHash(hashOfDistributionDate);
        if (dailyFundDistributionData == null) {
            return ResponseEntity.badRequest().body(new Response(DISTRIBUTION_DATE_ERROR, STATUS_ERROR));
        }

        LinkedHashMap<Hash, FundDistributionData> fundDistributionEntries = dailyFundDistributionData.getFundDistributionEntries();
        if (fundDistributionEntries == null || fundDistributionEntries.isEmpty()) {
            return ResponseEntity.badRequest().body(new Response(DISTRIBUTION_DATE_EMPTY_ENTRIES_ERROR, STATUS_ERROR));
        }
        FundDistributionData fundDistributionData = fundDistributionEntries.get(updateDistributionAmountRequest.getDistributionHash());
        if (fundDistributionData == null) {
            return ResponseEntity.badRequest().body(new Response(DISTRIBUTION_HASH_DOESNT_EXIST, STATUS_ERROR));
        }
        if (!fundDistributionData.isLockingAmount()) {
            return ResponseEntity.badRequest().body(new Response(DISTRIBUTION_INITIATED_OR_CANCELLED, STATUS_ERROR));
        }

        BigDecimal oldAmount = fundDistributionData.getAmount();
        if (!updateFundAvailableLockedBalances(fundDistributionData.getDistributionPoolFund().getFundHash(), fundDistributionData.getReceiverAddress(), updateDistributionAmountRequest.getDistributionAmount().subtract(oldAmount), true)) {
            return ResponseEntity.badRequest().body(new Response(INVALID_UPDATED_DISTRIBUTION_AMOUNT, STATUS_ERROR));
        }
        fundDistributionData.setAmount(updateDistributionAmountRequest.getDistributionAmount());
        dailyFundDistributions.put(dailyFundDistributionData);

        return ResponseEntity.ok().body(new UpdateDistributionAmountResponse(fundDistributionData.getHash(), oldAmount, fundDistributionData.getAmount()));
    }
}
