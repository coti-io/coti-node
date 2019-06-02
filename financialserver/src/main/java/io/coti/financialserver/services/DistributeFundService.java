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
import io.coti.financialserver.http.data.FundDistributionBalanceResponseData;
import io.coti.financialserver.http.data.FundDistributionFileData;
import io.coti.financialserver.http.data.FundDistributionResponseData;
import io.coti.financialserver.model.DailyFundDistributions;
import io.coti.financialserver.model.DailyFundDistributionFiles;
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
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class DistributeFundService {

    private static final int NUMBER_OF_DISTRIBUTION_LINE_DETAILS = 7;
    private static final int NUMBER_OF_DISTRIBUTION_SIGNATURE_LINE_DETAILS = 2;
    private static final String DAILY_DISTRIBUTION_RESULT_FILE_PREFIX = "distribution_results_";
    private static final String DAILY_DISTRIBUTION_RESULT_FILE_SUFFIX = ".csv";
    private static final String COMMA_SEPARATOR = ",";

    @Value("${financialserver.seed}")
    private String seed;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;
    @Autowired
    TransactionCreationService transactionCreationService;
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
    private Map<Hash, FundDistributionBalanceData> fundBalanceMap;

    public void initReservedBalance() {
        fundBalanceMap = new ConcurrentHashMap<>();
        for (Fund fund : Fund.values()) {
            Hash fundAddress = (fund.getFundHash() == null) ? getFundAddressHash(fund) : fund.getFundHash();
            FundDistributionBalanceData fundDistributionBalanceData = new FundDistributionBalanceData(fund, BigDecimal.ZERO);
            fundBalanceMap.put(fundAddress, fundDistributionBalanceData);
        }
        updateReservedAmountsFromPendingTransactions();
    }

    private void updateReservedAmountsFromPendingTransactions() {
        dailyFundDistributions.forEach(dailyFundDistributionData -> {
            dailyFundDistributionData.getFundDistributionEntries().values().forEach(fundDistributionData -> {
                if (fundDistributionData.isLockingAmount()) {
                    Hash fundAddress = (fundDistributionData.getDistributionPoolFund().getFundHash() == null) ?
                            getFundAddressHash(fundDistributionData.getDistributionPoolFund()) :
                            fundDistributionData.getDistributionPoolFund().getFundHash();
                    BigDecimal updatedLockedAmount = fundBalanceMap.get(fundAddress).getReservedAmount().add(fundDistributionData.getAmount());
                    fundBalanceMap.get(fundAddress).setReservedAmount(updatedLockedAmount);
                }
            });
        });


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
        fundBalanceMap.values().forEach(fundDistributionBalanceData -> {
            Hash fundAddress = (fundDistributionBalanceData.getFund().getFundHash() == null) ?
                    getFundAddressHash(fundDistributionBalanceData.getFund()) : fundDistributionBalanceData.getFund().getFundHash();
            fundDistributionBalanceResultDataList.add(
                    new FundDistributionBalanceResultData(fundDistributionBalanceData.getFund().getText(),
                            baseNodeBalanceService.getBalanceByAddress(fundAddress),
                            baseNodeBalanceService.getPreBalanceByAddress(fundAddress),
                            fundDistributionBalanceData.getReservedAmount()));
        });
        return ResponseEntity.status(HttpStatus.OK)
                .body(new FundDistributionBalanceResponse(new FundDistributionBalanceResponseData(fundDistributionBalanceResultDataList)));
    }


    public ResponseEntity<IResponse> distributeFundFromFile(FundDistributionRequest request) {
        // Verify whether any previous file was already handled
        Instant yesterdayInstant = getYesterdayInstant();
        Hash hashOfYesterday = getHashOfDate(yesterdayInstant);

        DailyFundDistributionFileData fundDistributionFileByDayByHash = dailyFundDistributionFiles.getByHash(hashOfYesterday);
        if (fundDistributionFileByDayByHash != null) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new Response(DISTRIBUTION_FILE_ALREADY_PROCESSED, STATUS_ERROR));
        }

        List<FundDistributionData> fundDistributionFileDataEntries = new ArrayList<>();
        ResponseEntity<IResponse> distributionFileVerificationResponse = verifyDailyDistributionFile(request, fundDistributionFileDataEntries);
        if (!distributionFileVerificationResponse.getStatusCode().equals(HttpStatus.OK))
            return distributionFileVerificationResponse;

        // Add new entries according to dates
        ResponseEntity<IResponse> responseEntity = updateWithTransactionsEntriesFromVerifiedFile(fundDistributionFileDataEntries);

        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            // File was verified and handled, update DB with file name in Hash of today's date
            String fileName = request.getFileName();
            DailyFundDistributionFileData fundDistributionFileOfDay = new DailyFundDistributionFileData(yesterdayInstant, fileName);
            if (dailyFundDistributionFiles.getByHash(hashOfYesterday) != null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(DISTRIBUTION_FILE_DATE_MISMATCHED, STATUS_ERROR));
            } else {
                dailyFundDistributionFiles.put(fundDistributionFileOfDay);
            }
        }

        return responseEntity;
    }

    public Instant getYesterdayInstant() {
        Instant todayInstant = Instant.now();
        long secondsInDay = 86400;
        return todayInstant.minusSeconds(secondsInDay);
    }

    public ResponseEntity<IResponse> verifyDailyDistributionFile(FundDistributionRequest request, List<FundDistributionData> fundDistributionFileDataEntries) {
        FundDistributionFileData fundDistributionFileData = request.getFundDistributionFileData(new Hash(kycServerPublicKey));
        String fileName = request.getFileName();

        ResponseEntity<IResponse> response = verifyDailyDistributionFileByName(fundDistributionFileDataEntries, fundDistributionFileData, fileName);
        if (response != null) return response;

        return ResponseEntity.status(HttpStatus.OK).body(new Response(INTERNAL_ERROR, STATUS_SUCCESS));
    }

    public ResponseEntity<IResponse> verifyDailyDistributionFileByName(List<FundDistributionData> fundDistributionFileDataEntries, FundDistributionFileData fundDistributionFileData, String fileName) {
        try {
            awsService.downloadFundDistributionFile(fileName);
        } catch (IOException e) {
            log.error(CANT_SAVE_FILE_ON_DISK, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(CANT_SAVE_FILE_ON_DISK, STATUS_ERROR));
        }

        ResponseEntity<IResponse> responseEntityForFileHandling = handleFundDistributionFile(fundDistributionFileData, fileName, fundDistributionFileDataEntries);
        if (!responseEntityForFileHandling.getStatusCode().equals(HttpStatus.OK))
            return responseEntityForFileHandling;

        // Verify signature of Request
        if (fundDistributionFileData.getUserSignature() == null || !fundDistributionFileCrypto.verifySignature(fundDistributionFileData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        return null;
    }

    public ResponseEntity<IResponse> handleFundDistributionFile(FundDistributionFileData fundDistributionFileData, String fileName, List<FundDistributionData> fundDistributionEntriesData) {
        // Parse file and process each line as a new Initial type transaction
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
                        BigInteger rHex = new BigInteger(distributionDetails[0], 16);
                        BigInteger sHex = new BigInteger(distributionDetails[1], 16);
                        fundDistributionFileData.setSignature(new SignatureData(rHex.toString(16), sHex.toString(16)));
                    } else {
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new Response(line, PARSED_WITH_ERROR));
                    }
                } else {
                    FundDistributionData entryData = handleFundDistributionFileLine(fundDistributionFileData, distributionDetails, fileName);
                    if (entryData != null) {
                        fundDistributionEntriesData.add(entryData);
                    } else {
                        log.error(BAD_CSV_FILE_LINE_FORMAT);
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new Response(line, BAD_CSV_FILE_LINE_FORMAT));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Errors on distribution funds service: {}", e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new Response(line, BAD_CSV_FILE_LINE_FORMAT));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(PARSED_SUCCESSFULLY, STATUS_SUCCESS));
    }

    private FundDistributionData handleFundDistributionFileLine(FundDistributionFileData fundDistributionFileData, String[] distributionDetails, String fileName) {
        // Load details for new transaction
        long id = Long.parseLong(distributionDetails[0]);
        Hash receiverAddress = new Hash(distributionDetails[1]);
        if (receiverAddress.toString().isEmpty())
            return null;
        String distributionPool = distributionDetails[2];   // Fund to spend
        BigDecimal amount = new BigDecimal(distributionDetails[3]);
        if (distributionDetails[4] == null || distributionDetails[5] == null)
            return null;
        Instant createTime = Instant.parse(distributionDetails[4]);
        Instant transactionTime = Instant.parse(distributionDetails[5]);
        String transactionDescription = distributionDetails[6];

        FundDistributionData entryData =
                new FundDistributionData(id, receiverAddress, Fund.getFundByText(distributionPool),
                        amount, createTime, transactionTime, transactionDescription);

        // Update signature message of file according to current line
        byte[] receiverAddressInBytes = receiverAddress.getBytes();
        byte[] distributionPoolInBytes = distributionPool.getBytes();
        byte[] amountInBytes = amount.stripTrailingZeros().toPlainString().getBytes();

        byte[] transactionDescriptionInBytes = transactionDescription.getBytes();

        byte[] entryDataInBytes = ByteBuffer.allocate(Long.BYTES + receiverAddressInBytes.length + distributionPoolInBytes.length
                + amountInBytes.length + Long.BYTES + Long.BYTES + transactionDescriptionInBytes.length)
                .putLong(id).put(receiverAddressInBytes).put(distributionPoolInBytes).put(amountInBytes)
                .putLong(createTime.toEpochMilli()).putLong(transactionTime.toEpochMilli()).put(transactionDescriptionInBytes)
                .array();
        fundDistributionFileData.getSignatureMessage().add(entryDataInBytes);
        fundDistributionFileData.incrementMessageByteSize(entryDataInBytes.length);

        entryData.initHashes();
        entryData.setFileName(fileName);
        return entryData;
    }

    private ResponseEntity<IResponse> updateWithTransactionsEntriesFromVerifiedFile(List<FundDistributionData> fundDistributionFileEntriesData) {
        List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList = new ArrayList<>();
        fundDistributionFileEntriesData.forEach(entryData -> {
                    boolean accepted = false;
                    boolean passedPreBalanceCheck = false;
                    boolean isLockupDateValid = isLockupDateValid(entryData);
                    boolean uniqueByDate = isEntryDataUniquePerDate(entryData);

                    if (isLockupDateValid && uniqueByDate && updateFundsAvailableLockedBalance(entryData)) {
                        // Verified uniqueness and pre-balance, add it to structure by dates
                        entryData.setStatus(DistributionEntryStatus.ACCEPTED);
                        if (dailyFundDistributions.getByHash(entryData.getHashByDate()) == null) {
                            LinkedHashMap<Hash, FundDistributionData> fundDistributionEntries = new LinkedHashMap<>();
                            DailyFundDistributionData fundDistributionByDateData = new DailyFundDistributionData(entryData.getTransactionTime(), fundDistributionEntries);
                            dailyFundDistributions.put(fundDistributionByDateData);
                        }

                        DailyFundDistributionData fundDistributionOfDay = dailyFundDistributions.getByHash(entryData.getHashByDate());
                        fundDistributionOfDay.getFundDistributionEntries().put(entryData.getHash(), entryData);
                        dailyFundDistributions.put(fundDistributionOfDay);
                        accepted = true;
                        passedPreBalanceCheck = true;
                    }
                    String statusByChecks = getTransactionEntryStatusByChecks(accepted, passedPreBalanceCheck, uniqueByDate, isLockupDateValid);
                    fundDistributionFileEntryResultDataList.add(new FundDistributionFileEntryResultData(entryData.getId(), entryData.getReceiverAddress(),
                            entryData.getDistributionPoolFund().getText(), entryData.getSource(), accepted, statusByChecks));
                }
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(new FundDistributionResponse(new FundDistributionResponseData(fundDistributionFileEntryResultDataList)));
    }

    private String getTransactionEntryStatusByChecks(boolean accepted, boolean passedPreBalanceCheck, boolean uniqueByDate, boolean isLockupDateValid) {
        if (accepted)
            return ACCEPTED;
        if (!isLockupDateValid)
            return LOCK_UP_DATE_IS_INVALID;
        if (!uniqueByDate)
            return DATE_UNIQUENESS_WAS_NOT_MAINTAINED;
        if (!passedPreBalanceCheck)
            return DISTRIBUTION_POOL_BALANCE_CHECKS_FAILED;
        return null;
    }


    private boolean updateFundsAvailableLockedBalance(FundDistributionData entryData) {
        if (entryData.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            // Illegal non positive amount
            return false;
        }
        Hash fundAddress = getFundAddressHash(entryData.getDistributionPoolFund());
        FundDistributionBalanceData fundDistributionBalanceData = fundBalanceMap.get(fundAddress);
        BigDecimal updatedAmountToLock = fundDistributionBalanceData.getReservedAmount().add(entryData.getAmount());
        if (updatedAmountToLock.compareTo(baseNodeBalanceService.getPreBalanceByAddress(fundAddress)) > 0 ||
                updatedAmountToLock.compareTo(baseNodeBalanceService.getBalanceByAddress(fundAddress)) > 0) {   // Not enough money in pre-balance
            return false;
        } else {
            fundDistributionBalanceData.setReservedAmount(updatedAmountToLock);
        }
        return true;
    }

    private boolean isEntryDataUniquePerDate(FundDistributionData entryData) {
        //Verify no duplicate source->target transactions are scheduled for the same date
        Instant transactionReleaseDate = entryData.getTransactionTime();
        Hash hashOfDate = getHashOfDate(transactionReleaseDate);
        if (dailyFundDistributions.getByHash(hashOfDate) != null &&
                dailyFundDistributions.getByHash(hashOfDate).getFundDistributionEntries().get(entryData.getHash()) != null)
            return false;
        return true;
    }

    private boolean isLockupDateValid(FundDistributionData entryData) {
        // Verify lock-up date is not null nor prior to yesterday
        Instant transactionReleaseDate = entryData.getTransactionTime();
        if (transactionReleaseDate == null)
            return false;
        Instant yesterdayInstant = getYesterdayInstant();
        if (transactionReleaseDate.compareTo(yesterdayInstant) < 0 &&
                !getHashOfDate(transactionReleaseDate).equals(getHashOfDate(yesterdayInstant)))
            return false;
        return true;
    }


    @Scheduled(cron = "0 0 3 * * *")
    public void scheduleTaskUsingCronExpression() {
        log.info("Starting scheduled action for creating pending transactions");
        ResponseEntity<IResponse> pendingTransactionsResponse = createPendingTransactions();
        if (pendingTransactionsResponse.getStatusCode() != HttpStatus.OK) {
            log.error("Scheduled task of creating pending transactions failed");
        } else {
            log.info("Finished scheduled action for creating pending transactions");
        }
    }

    private ResponseEntity<IResponse> createPendingTransactions() {
        List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList = new ArrayList<>();
        // Failed transactions should be ran before the non failed ones in order to avoid trying to run them twice
        createPendingFailedTransactions(fundDistributionFileEntryResultDataList);
        createPendingNonFailedTransactionsByDate(fundDistributionFileEntryResultDataList);
        return createFundDistributionFileResult(fundDistributionFileEntryResultDataList);
    }

    private void createPendingNonFailedTransactionsByDate(List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList) {
        Hash hashOfYesterday = getHashOfDate(getYesterdayInstant());
        if (dailyFundDistributions.getByHash(hashOfYesterday) == null)
            return;

        DailyFundDistributionData dailyFundDistributionDataOfToday = dailyFundDistributions.getByHash(hashOfYesterday);
        LinkedHashMap<Hash, FundDistributionData> fundDistributionEntriesOfToday = dailyFundDistributionDataOfToday.getFundDistributionEntries();
        for (FundDistributionData fundDistributionFileEntry : dailyFundDistributions.getByHash(hashOfYesterday).getFundDistributionEntries().values()) {
            boolean isSuccessful = false;
            Hash initialTransactionHash = null;
            // Create a new Initial transaction if status allows it
            if (fundDistributionFileEntry.isReadyToInitiate()) {
                initialTransactionHash = createInitialTransactionToDistributionEntry(fundDistributionFileEntry);
                if (initialTransactionHash != null) {
                    // Update DB with new transaction
                    isSuccessful = true;
                    fundDistributionFileEntry.setStatus(DistributionEntryStatus.CREATED);
                    fundDistributionEntriesOfToday.put(fundDistributionFileEntry.getHash(), fundDistributionFileEntry);
                    updateReservedBalanceAfterTransactionCreated(fundDistributionFileEntry);
                } else {
                    fundDistributionFileEntry.setStatus(DistributionEntryStatus.FAILED);
                    fundDistributionEntriesOfToday.put(fundDistributionFileEntry.getHash(), fundDistributionFileEntry);
                    FailedFundDistributionData fundDistributionFailedHashesOfToday = failedFundDistributions.getByHash(hashOfYesterday);
                    if (fundDistributionFailedHashesOfToday == null)
                        fundDistributionFailedHashesOfToday = new FailedFundDistributionData(hashOfYesterday);
                    fundDistributionFailedHashesOfToday.getFundDistributionHashes().put(fundDistributionFileEntry.getHash(), fundDistributionFileEntry.getHash());
                    failedFundDistributions.put(fundDistributionFailedHashesOfToday);
                }
                dailyFundDistributions.put(dailyFundDistributionDataOfToday);
                String status = isSuccessful ? TRANSACTION_CREATED_SUCCESSFULLY : TRANSACTION_CREATION_FAILED;
                fundDistributionFileEntryResultDataList.add(new FundDistributionFileEntryResultData(fundDistributionFileEntry.getId(),
                        fundDistributionFileEntry.getReceiverAddress(), fundDistributionFileEntry.getDistributionPoolFund().getText(),
                        fundDistributionFileEntry.getSource(), isSuccessful, status));
            }
        }
    }

    private void updateReservedBalanceAfterTransactionCreated(FundDistributionData fundDistributionFileEntry) {
        // Update reserved balance, once transaction is created
        FundDistributionBalanceData fundReserveBalanceData = fundBalanceMap.get(fundDistributionFileEntry.getDistributionPoolFund().getFundHash());
        BigDecimal updatedReservedAmount = fundReserveBalanceData.getReservedAmount().subtract(fundDistributionFileEntry.getAmount());
        if (updatedReservedAmount.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Reserved amount can not be negative.");
            fundReserveBalanceData.setReservedAmount(BigDecimal.ZERO);
        } else {
            fundReserveBalanceData.setReservedAmount(updatedReservedAmount);
        }
    }

    private Hash getHashOfDate(Instant dayInstant) {
        LocalDateTime ldt = LocalDateTime.ofInstant(dayInstant, ZoneOffset.UTC);
        return CryptoHelper.cryptoHash((ldt.getYear() + ldt.getMonth().toString() +
                ldt.getDayOfMonth()).getBytes());
    }

    private void createPendingFailedTransactions(List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList) {
        failedFundDistributions.forEach(distributionFailedHashes ->
        {
            Hash hashOfDay = distributionFailedHashes.getHash();
            DailyFundDistributionData fundDistributionDataOfToday = dailyFundDistributions.getByHash(hashOfDay);
            for (Iterator<Hash> failedEntryHashKeys = distributionFailedHashes.getFundDistributionHashes().keySet().iterator(); failedEntryHashKeys.hasNext(); ) {
                Hash failedEntryHashKey = failedEntryHashKeys.next();
                Hash initialTransactionHash = null;
                boolean isSuccessful = false;
                FundDistributionData entryData = fundDistributionDataOfToday.getFundDistributionEntries().get(failedEntryHashKey);
                if (entryData.getStatus().equals(DistributionEntryStatus.FAILED)) {
                    initialTransactionHash = createInitialTransactionToDistributionEntry(entryData);
                    if (initialTransactionHash != null) {
                        // Update DB with new transaction
                        isSuccessful = true;
                        entryData.setStatus(DistributionEntryStatus.CREATED);
                        failedEntryHashKeys.remove();
                        // Update reserved balance, once transaction is created
                        updateReservedBalanceAfterTransactionCreated(entryData);
                    }
                    String status = isSuccessful ? TRANSACTION_CREATED_SUCCESSFULLY : TRANSACTION_CREATION_FAILED;
                    fundDistributionFileEntryResultDataList.add(new FundDistributionFileEntryResultData(entryData.getId(), entryData.getReceiverAddress(),
                            entryData.getDistributionPoolFund().getText(), entryData.getSource(), isSuccessful, status));
                } else {
                    failedEntryHashKeys.remove();
                }
            }
            dailyFundDistributions.put(fundDistributionDataOfToday);
            failedFundDistributions.put(distributionFailedHashes);
        });
    }

    private String createDistributionResultFileNameForToday() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        String today = ldt.getYear() + "-" + StringUtils.leftPad("" + ldt.getMonthValue(), 2, "0") + "-" + ldt.getDayOfMonth();
        return DAILY_DISTRIBUTION_RESULT_FILE_PREFIX + today + DAILY_DISTRIBUTION_RESULT_FILE_SUFFIX;
    }

    private Hash getEntryResultSourceFundAddress(FundDistributionFileEntryResultData entryResult) {
        int sourceAddressIndex = Math.toIntExact(Fund.getFundByText(entryResult.getDistributionPool()).getReservedAddress().getIndex());
        return nodeCryptoHelper.generateAddress(seed, sourceAddressIndex);
    }

    private Hash createInitialTransactionToDistributionEntry(FundDistributionData fundDistributionFileEntry) {
        Hash initialTransactionHash = null;
        try {
            int sourceAddressIndex = Math.toIntExact(fundDistributionFileEntry.getDistributionPoolFund().getReservedAddress().getIndex());
            Hash sourceAddress = nodeCryptoHelper.generateAddress(seed, sourceAddressIndex);
            initialTransactionHash = transactionCreationService.createInitialTransactionToFund(fundDistributionFileEntry.getAmount(),
                    sourceAddress, fundDistributionFileEntry.getReceiverAddress(), sourceAddressIndex);
        } catch (Exception e) {
            log.error("Failed to create transaction ", e);
        }
        return initialTransactionHash;
    }

    private ResponseEntity<IResponse> createFundDistributionFileResult(List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList) {
        // Create results file locally according to given fileNameForToday, Sign file and upload it to S3
        String resultsFileNameForToday = createDistributionResultFileNameForToday();
        File file = new File(resultsFileNameForToday);
        // Create results file based on fundDistributionFileEntryResultDataList including signature
        FundDistributionFileResultData fundDistributionFileResultData = new FundDistributionFileResultData();
        try (Writer fileWriter = new FileWriter(resultsFileNameForToday, false)) {
            for (FundDistributionFileEntryResultData entryResult : fundDistributionFileEntryResultDataList) {
                fileWriter.write(getEntryResultAsCommaDelimitedLine(entryResult));
                updateFundDistributionFileResultData(fundDistributionFileResultData, entryResult);
            }
            fundDistributionFileResultData.setFinancialServerHash(networkService.getNetworkNodeData().getNodeHash());
            fundDistributionFileResultCrypto.signMessage(fundDistributionFileResultData);
            SignatureData signature = fundDistributionFileResultCrypto.getSignature(fundDistributionFileResultData);
            fileWriter.write(signature.getR() + COMMA_SEPARATOR + signature.getS()
                    + COMMA_SEPARATOR + fundDistributionFileResultData.getFinancialServerHash());
        } catch (IOException e) {
            log.error(CANT_SAVE_FILE_ON_DISK, e);
        }
        // Upload file to S3
        awsService.uploadFundDistributionResultFile(resultsFileNameForToday, file, "application/vnd.ms-excel");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new FundDistributionResponse(new FundDistributionResponseData(fundDistributionFileEntryResultDataList)));
    }

    private String getEntryResultAsCommaDelimitedLine(FundDistributionFileEntryResultData entryResult) {
        return Long.toString(entryResult.getId()) + COMMA_SEPARATOR + entryResult.getDistributionPool() + COMMA_SEPARATOR +
                entryResult.getSource() + COMMA_SEPARATOR + getEntryResultSourceFundAddress(entryResult).toString() + COMMA_SEPARATOR +
                entryResult.getReceiverAddress().toString() + COMMA_SEPARATOR + ((Boolean) entryResult.isAccepted()).toString() + COMMA_SEPARATOR +
                entryResult.getStatus() + "\n";
    }

    private void updateFundDistributionFileResultData(FundDistributionFileResultData fundDistributionFileResultData, FundDistributionFileEntryResultData entryResult) {
        byte[] distributionPoolNameInBytes = entryResult.getDistributionPool().getBytes();
        byte[] sourceInBytes = entryResult.getSource().getBytes();
        byte[] distributionPoolAddressInBytes = entryResult.getReceiverAddress().getBytes();
        byte[] receiverAddressInBytes = entryResult.getReceiverAddress().getBytes();
        byte[] isAcceptedInBytes = ((Boolean) entryResult.isAccepted()).toString().getBytes();
        byte[] statusInBytes = entryResult.getStatus().getBytes();

        byte[] resultLineInBytes = ByteBuffer.allocate(Long.BYTES + distributionPoolNameInBytes.length + sourceInBytes.length + distributionPoolAddressInBytes.length +
                receiverAddressInBytes.length + isAcceptedInBytes.length + statusInBytes.length).
                putLong(entryResult.getId()).put(distributionPoolNameInBytes).put(sourceInBytes).put(distributionPoolAddressInBytes).put(receiverAddressInBytes).
                put(statusInBytes).array();

        fundDistributionFileResultData.getSignatureMessage().add(resultLineInBytes);
        fundDistributionFileResultData.incrementMessageByteSize(resultLineInBytes.length);
    }


}
