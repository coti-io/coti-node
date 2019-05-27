package io.coti.financialserver.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.financialserver.crypto.FundDistributionFileCrypto;
import io.coti.financialserver.crypto.FundDistributionFileResultCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.*;
import io.coti.financialserver.http.data.FundDistributionResponseData;
import io.coti.financialserver.http.data.FundDistributionBalanceResponseData;
import io.coti.financialserver.http.data.FundDistributionFileData;
import io.coti.financialserver.model.*;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class DistributeFundService {
    public static final String CANT_SAVE_FILE_ON_DISK = "Can't save file on disk.";
    public static final String CANT_LOAD_FILE_FROM_SHARED_LOCATION = "Can't load file from shared location.";
    private static final int NUMBER_OF_DISTRIBUTION_LINE_DETAILS = 6;
    private static final int NUMBER_OF_DISTRIBUTION__SIGNATURE_LINE_DETAILS = 2;
    private static final String BAD_CSV_FILE_FORMAT = "Bad csv file format";
    private static final String BAD_CSV_FILE_LINE_FORMAT = "Bad csv file line format";
    private static final String DAILY_DISTRIBUTION_RESULT_FILE_PREFIX = "distribution_results_";
    private static final String DAILY_DISTRIBUTION_RESULT_FILE_SUFFIX = ".csv";
    public static final String COMMA_SEPARATOR = ",";

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
    private DailyFundDistribution dailyFundDistribution;
    @Autowired
    private FailedFundDistribution failedFundDistribution;
    @Autowired
    private DailyFundDistributionFile dailyFundDistributionFile;


    private Map<Hash, FundDistributionBalanceData> fundBalanceMap;

    public void initReservedBalance() {
        fundBalanceMap = new ConcurrentHashMap<>();
        for (Fund fund : Fund.values()) {
            Hash fundAddress = getFundAddressHash(fund);
            fund.setFundHash(fundAddress);
            FundDistributionBalanceData fundDistributionBalanceData = new FundDistributionBalanceData(fund, BigDecimal.ZERO);
            fundDistributionBalanceData.setPreBalance(baseNodeBalanceService.getPreBalanceByAddress(fundAddress));
            fundDistributionBalanceData.setBalance(baseNodeBalanceService.getBalanceByAddress(fundAddress));
            fundBalanceMap.put(fundAddress, fundDistributionBalanceData);
        }
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
        return getCurrentFundsBalances();
    }

    private ResponseEntity<IResponse> getCurrentFundsBalances() {
        List<FundDistributionBalanceResultData> fundDistributionBalanceResultDataList = new ArrayList<>();
        fundBalanceMap.values().forEach(fundDistributionBalanceData ->{
            fundDistributionBalanceResultDataList.add(
                new FundDistributionBalanceResultData(fundDistributionBalanceData.getFund().getText(), fundDistributionBalanceData.getBalance(),
                        fundDistributionBalanceData.getPreBalance(), fundDistributionBalanceData.getReservedAmount()));
        });
        return ResponseEntity.status(HttpStatus.OK)
                .body(new FundDistributionBalanceResponse(new FundDistributionBalanceResponseData(fundDistributionBalanceResultDataList)));
    }








    public ResponseEntity<IResponse> distributeFundFromFile(FundDistributionRequest request)  {
        // Verify whether any previous file was already handled
        Instant todayInstant = Instant.now();
        Hash hashOfToday = getHashOfDate(todayInstant);
        DailyFundDistributionFileData fundDistributionFileByDayByHash = dailyFundDistributionFile.getByHash(hashOfToday);
        //TODO:  for testing purposes commented below
//        if( fundDistributionFileByDayByHash != null ) {
//            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new Response(DISTRIBUTION_FILE_ALREADY_PROCESSED, STATUS_ERROR));
//        }

        List<FundDistributionData> fundDistributionFileDataEntries = new ArrayList<>();
        ResponseEntity<IResponse> distributionFileVerificationResponse =  verifyDailyDistributionFile(request, fundDistributionFileDataEntries);
        if( !distributionFileVerificationResponse.getStatusCode().equals(HttpStatus.OK) )
            return distributionFileVerificationResponse;

        // Add new entries according to dates
        ResponseEntity<IResponse> responseEntity = updateWithTransactionsEntriesFromVerifiedFile(fundDistributionFileDataEntries);

        if(responseEntity.getStatusCode().equals(HttpStatus.OK)){
            // File was verified and handled, update DB with file name in Hash of today's date
            String fileName = request.getFileName();
            DailyFundDistributionFileData fundDistributionFileOfToday = new DailyFundDistributionFileData(todayInstant, fileName);
            fundDistributionFileOfToday.initHashByDate();
            //TODO:  for testing purposes commented below
//            if( dailyFundDistributionFile.getByHash(hashOfToday) != null ) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(DISTRIBUTION_FILE_DATE_MISMATCHED, STATUS_ERROR));
//            } else {
                dailyFundDistributionFile.put(fundDistributionFileOfToday);
//            }
        }

        // TODO: the method should end here, next call is to be made from call of Financial server scheduler
        ResponseEntity<IResponse> pendingTransactionsResponse = createPendingTransactions();

        return responseEntity;
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
        if( !responseEntityForFileHandling.getStatusCode().equals(HttpStatus.OK) )
                return responseEntityForFileHandling;

//         Verify signature of Request //TODO:  for testing purposes commented below signature check
//        if( !fundDistributionFileCrypto.verifySignature(fundDistributionFileData) ) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
//        }
        return null;
    }

    public ResponseEntity<IResponse> handleFundDistributionFile(FundDistributionFileData fundDistributionFileData, String fileName, List<FundDistributionData> fundDistributionEntriesData) {
        // Parse file and process each line as a new Initial type transaction
        String line = "";
        try( BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if( line.isEmpty() )
                    break;
                String[] distributionDetails;
                distributionDetails = line.split(COMMA_SEPARATOR);
                if( distributionDetails.length != NUMBER_OF_DISTRIBUTION_LINE_DETAILS) {
                    if(distributionDetails.length == NUMBER_OF_DISTRIBUTION__SIGNATURE_LINE_DETAILS)
                    {
                        fundDistributionFileData.setSignature(new SignatureData(distributionDetails[0], distributionDetails[1]));
                    } else {
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new Response(line, PARSED_WITH_ERROR));
                    }
                } else {
                    FundDistributionData entryData = handleFundDistributionFileLine(fundDistributionFileData, distributionDetails, fileName);
                    if( entryData != null)
                    {
                        fundDistributionEntriesData.add(entryData);
                    } else {
                        log.error(BAD_CSV_FILE_LINE_FORMAT);
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new Response( line, BAD_CSV_FILE_LINE_FORMAT));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Errors on distribution funds service: {}", e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new Response(line, BAD_CSV_FILE_LINE_FORMAT ));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(PARSED_SUCCESSFULLY, STATUS_SUCCESS));
    }

    private FundDistributionData handleFundDistributionFileLine(FundDistributionFileData fundDistributionFileData, String[] distributionDetails, String fileName) {
        // Load details for new transaction
        Hash receiverAddress = new Hash(distributionDetails[0]);
        if( receiverAddress.toString().isEmpty() )
            return null;
        String distributionPool = distributionDetails[1];   // Fund to spend
        BigDecimal amount = new BigDecimal(distributionDetails[2]);
        if( distributionDetails[3] == null || distributionDetails[4] == null)
            return null;
        Instant createTime = Instant.parse(distributionDetails[3]);
        Instant transactionTime  = Instant.parse(distributionDetails[4]);
        String transactionDescription = distributionDetails[5];

        FundDistributionData entryData =
                new FundDistributionData(receiverAddress,Fund.getFundByText(distributionPool),
                        amount,createTime,transactionTime,transactionDescription);

        // Update signature message of file according to current line
        byte[] receiverAddressInBytes = receiverAddress.getBytes();
        byte[] distributionPoolInBytes = distributionPool.getBytes();
        byte[] amountInBytes = amount.stripTrailingZeros().toPlainString().getBytes();

        byte[] transactionDescriptionInBytes = transactionDescription.getBytes();

        byte[] entryDataInBytes = ByteBuffer.allocate(receiverAddressInBytes.length + distributionPoolInBytes.length +amountInBytes.length
                + Long.BYTES + Long.BYTES + transactionDescriptionInBytes.length)
                .put(receiverAddressInBytes).put(distributionPoolInBytes).put(amountInBytes)
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
                    boolean uniqueByDate = isEntryDateUniquePerDate(entryData);

                    if( uniqueByDate && updateFundsAvailableLockedBalance(entryData) ){
                        // Verified uniqueness and pre-balance, add it to structure by dates
                        entryData.setStatus(DistributionEntryStatus.ACCEPTED);
                        if(dailyFundDistribution.getByHash(entryData.getHashByDate()) == null) {
                            Instant transactionReleaseInstant = (entryData.getTransactionTime() != null) ? entryData.getTransactionTime() : Instant.now();
                            transactionReleaseInstant = (transactionReleaseInstant.isAfter(Instant.now())) ? transactionReleaseInstant : Instant.now();
                            LinkedHashMap<Hash, FundDistributionData> fundDistributionEntries = new LinkedHashMap<>();
                            DailyFundDistributionData fundDistributionByDateData = new DailyFundDistributionData(transactionReleaseInstant, fundDistributionEntries);
                            fundDistributionByDateData.initHashByDate();
                            dailyFundDistribution.put(fundDistributionByDateData);
                        }

                        DailyFundDistributionData fundDistributionOfDay = dailyFundDistribution.getByHash(entryData.getHashByDate());
                        fundDistributionOfDay.getFundDistributionEntries().put(entryData.getHash(), entryData);
                        dailyFundDistribution.put(fundDistributionOfDay);
                        accepted = true;
                        passedPreBalanceCheck = true;
                    }
                    fundDistributionFileEntryResultDataList.add(new FundDistributionFileEntryResultData(entryData.getReceiverAddress(),
                            entryData.getDistributionPoolFund().getText(), entryData.getSource(), accepted, passedPreBalanceCheck, uniqueByDate));
                }
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(new FundDistributionResponse(new FundDistributionResponseData(fundDistributionFileEntryResultDataList)));
    }


    private boolean updateFundsAvailableLockedBalance(FundDistributionData entryData) {
        if(entryData.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            // Illegal non positive amount
            return false;
        }
        Hash fundAddress = getFundAddressHash(entryData.getDistributionPoolFund());
        FundDistributionBalanceData fundDistributionBalanceData = fundBalanceMap.get(fundAddress);
        BigDecimal updatedAmountToLock = fundDistributionBalanceData.getReservedAmount().add(entryData.getAmount());
        if( updatedAmountToLock.compareTo(fundDistributionBalanceData.getPreBalance()) > 0 ||
                updatedAmountToLock.compareTo(fundDistributionBalanceData.getBalance()) > 0 )
        {   // Not enough money in pre-balance
            return false;
        }
        else {
            fundDistributionBalanceData.setReservedAmount(updatedAmountToLock);
        }
        return true;
    }

    private boolean isEntryDateUniquePerDate(FundDistributionData entryData) {
        //Verify no duplicate source->target transactions are scheduled for the same date
        Instant transactionReleaseDate = entryData.getTransactionTime();
        if( transactionReleaseDate == null || Instant.now().isAfter(transactionReleaseDate))
            transactionReleaseDate = Instant.now();
        Hash hashOfDate = getHashOfDate(transactionReleaseDate);
        if(dailyFundDistribution.getByHash(hashOfDate) != null &&
                dailyFundDistribution.getByHash(hashOfDate).getFundDistributionEntries().get(entryData.getHash()) != null )
            return false;
        return true;
    }





    @Scheduled(cron = "0 0 23 * * *")
    public void scheduleTaskUsingCronExpression() {
        log.info("Starting scheduled action for creating pending transactions");
        ResponseEntity<IResponse> pendingTransactionsResponse = createPendingTransactions();
        if( pendingTransactionsResponse.getStatusCode()!= HttpStatus.OK) {
            log.error("Scheduled task of creating pending transactions failed");
        } else {
            log.info("Finished scheduled action for creating pending transactions");
        }
    }

    private ResponseEntity<IResponse> createPendingTransactions() {
        List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList = new ArrayList<>();

        createPendingNonFailedTransactionsByDate(fundDistributionFileEntryResultDataList);
        //TODO: Failed transactions should be ran before the non failed ones in order to avoid trying to run them twice, current order is just for testing of failed scenarios
        createPendingFailedTransactions(fundDistributionFileEntryResultDataList);

        return createFundDistributionFileResult(fundDistributionFileEntryResultDataList);
    }

    private void createPendingNonFailedTransactionsByDate(List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList) {
        Instant todayInstant = Instant.now();
        Hash hashOfToday = getHashOfDate(todayInstant);
        if( dailyFundDistribution.getByHash(hashOfToday) == null)
            return;

        DailyFundDistributionData dailyFundDistributionDataOfToday = dailyFundDistribution.getByHash(hashOfToday);
        LinkedHashMap<Hash, FundDistributionData> fundDistributionEntriesOfToday = dailyFundDistributionDataOfToday.getFundDistributionEntries();
        for (FundDistributionData fundDistributionFileEntry : dailyFundDistribution.getByHash(hashOfToday).getFundDistributionEntries().values())
        {
            boolean isSuccessful = false;
            Hash initialTransactionHash = null;
            // Create a new Initial transaction if status allows it
            if( fundDistributionFileEntry.isReadyToInitiate() ) {
//                if( !fundDistributionFileEntry.getDistributionPoolFund().getText().equals(Fund.ADVISORS.getText()) )   // TODO: for testing purposes to simulate failures
                    initialTransactionHash = createInitialTransactionToDistributionEntry(fundDistributionFileEntry);
                if( initialTransactionHash != null )
                {
                    // Update DB with new transaction
                    isSuccessful = true;
                    fundDistributionFileEntry.setStatus(DistributionEntryStatus.CREATED);
                    fundDistributionEntriesOfToday.put(fundDistributionFileEntry.getHash(), fundDistributionFileEntry);
                    updateReservedBalanceAfterTransactionCreated(fundDistributionFileEntry);
                } else {
                    fundDistributionFileEntry.setStatus(DistributionEntryStatus.FAILED);
                    fundDistributionEntriesOfToday.put(fundDistributionFileEntry.getHash(), fundDistributionFileEntry);
                    FailedFundDistributionData fundDistributionFailedHashesOfToday = failedFundDistribution.getByHash(hashOfToday);
                    if( fundDistributionFailedHashesOfToday == null)
                        fundDistributionFailedHashesOfToday = new FailedFundDistributionData(hashOfToday);
                    fundDistributionFailedHashesOfToday.getFundDistributionHashes().put(fundDistributionFileEntry.getHash(), fundDistributionFileEntry.getHash());
                    failedFundDistribution.put(fundDistributionFailedHashesOfToday);
                }
                dailyFundDistribution.put(dailyFundDistributionDataOfToday);
                fundDistributionFileEntryResultDataList.add(new FundDistributionFileEntryResultData(fundDistributionFileEntry.getReceiverAddress(),
                        fundDistributionFileEntry.getDistributionPoolFund().getText(), fundDistributionFileEntry.getSource(), isSuccessful, true, true));
            }
        }
    }

    private void updateReservedBalanceAfterTransactionCreated(FundDistributionData fundDistributionFileEntry) {
        // Update reserved balance, once transaction is created
        FundDistributionBalanceData fundReserveBalanceData = fundBalanceMap.get(fundDistributionFileEntry.getDistributionPoolFund().getFundHash());
        BigDecimal updatedReservedAmount = fundReserveBalanceData.getReservedAmount().subtract(fundDistributionFileEntry.getAmount());
        if( updatedReservedAmount.compareTo( BigDecimal.ZERO ) < 0 ) {
            log.error("Reserved amount can not be negative.");
            fundReserveBalanceData.setReservedAmount(BigDecimal.ZERO);
        } else {
            fundReserveBalanceData.setReservedAmount(updatedReservedAmount);
        }
    }

    private Hash getHashOfDate(Instant todayInstant) {
        LocalDateTime ldt = LocalDateTime.ofInstant(todayInstant, ZoneId.systemDefault());
        return CryptoHelper.cryptoHash( (ldt.getYear() + ldt.getMonth().toString() +
                ldt.getDayOfMonth()).getBytes() );
    }

    private void createPendingFailedTransactions(List<FundDistributionFileEntryResultData> fundDistributionFileEntryResultDataList) {
        failedFundDistribution.forEach(distributionFailedHashes ->
        {
            Hash hashOfDay = distributionFailedHashes.getHash();
            DailyFundDistributionData fundDistributionDataOfToday = dailyFundDistribution.getByHash(hashOfDay);
            for(Iterator<Hash> failedEntryHashKeys = distributionFailedHashes.getFundDistributionHashes().keySet().iterator(); failedEntryHashKeys.hasNext(); )
            {
                Hash failedEntryHashKey = failedEntryHashKeys.next();
                Hash initialTransactionHash = null;
                boolean isSuccessful = false;
                FundDistributionData entryData = fundDistributionDataOfToday.getFundDistributionEntries().get(failedEntryHashKey);
                if( entryData.getStatus().equals(DistributionEntryStatus.FAILED) )
                {
//                    if( !entryData.getDistributionPoolFund().getText().equals(Fund.ADVISORS.getText()) )   // TODO: for testing purposes to simulate failures
                        initialTransactionHash = createInitialTransactionToDistributionEntry(entryData);
                    if( initialTransactionHash != null ) {
                        // Update DB with new transaction
                        isSuccessful = true;
                        entryData.setStatus(DistributionEntryStatus.CREATED);
                        failedEntryHashKeys.remove();
                        // Update reserved balance, once transaction is created
                        updateReservedBalanceAfterTransactionCreated(entryData);
                    }
                    fundDistributionFileEntryResultDataList.add(new FundDistributionFileEntryResultData(entryData.getReceiverAddress(),
                            entryData.getDistributionPoolFund().getText(), entryData.getSource(), isSuccessful, true, true));
                } else {
                    failedEntryHashKeys.remove();
                }
            }
            dailyFundDistribution.put(fundDistributionDataOfToday);
            failedFundDistribution.put(distributionFailedHashes);
        });
    }

    private String createDistributionResultFileNameForToday() {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        String today = ldt.getYear() + "-" + StringUtils.leftPad(""+ldt.getMonthValue(),2,"0") + "-" + ldt.getDayOfMonth();
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
        try( Writer fileWriter = new FileWriter(resultsFileNameForToday, false) ) {
            for (FundDistributionFileEntryResultData entryResult : fundDistributionFileEntryResultDataList) {
                fileWriter.write( getEntryResultAsCommaDelimitedLine(entryResult) );
                updateFundDistributionFileResultData(fundDistributionFileResultData, entryResult);
            }
            fundDistributionFileResultData.setFinancialServerHash(networkService.getSingleNodeData(NodeType.ZeroSpendServer).getNodeHash());
            fundDistributionFileResultCrypto.signMessage(fundDistributionFileResultData);
            SignatureData signature = fundDistributionFileResultCrypto.getSignature(fundDistributionFileResultData);
            fileWriter.write( signature.getR() + COMMA_SEPARATOR + signature.getS() );
        } catch (IOException e) {
            log.error(CANT_SAVE_FILE_ON_DISK, e);
        }
        // Upload file to S3
        awsService.uploadFundDistributionResultFile(resultsFileNameForToday, file,"application/vnd.ms-excel");
        return ResponseEntity.status(HttpStatus.OK)
                .body(new FundDistributionResponse(new FundDistributionResponseData(fundDistributionFileEntryResultDataList)));
    }

    private String getEntryResultAsCommaDelimitedLine(FundDistributionFileEntryResultData entryResult) {
        return entryResult.getDistributionPool()+ COMMA_SEPARATOR + entryResult.getSource() + COMMA_SEPARATOR +
                getEntryResultSourceFundAddress(entryResult).toString()+COMMA_SEPARATOR+
                entryResult.getReceiverAddress().toString()+COMMA_SEPARATOR+ ((Boolean)entryResult.isAccepted()).toString()+COMMA_SEPARATOR+
                ((Boolean)entryResult.isUniqueByDate()).toString()+ COMMA_SEPARATOR + ((Boolean)entryResult.isPassedPreBalanceCheck()).toString()+"\n";
    }

    private void updateFundDistributionFileResultData(FundDistributionFileResultData fundDistributionFileResultData, FundDistributionFileEntryResultData entryResult) {
        byte[] distributionPoolNameInBytes = entryResult.getDistributionPool().getBytes();
        byte[] sourceInBytes = entryResult.getSource().getBytes();
        byte[] distributionPoolAddressInBytes =  entryResult.getReceiverAddress().getBytes();
        byte[] receiverAddressInBytes = entryResult.getReceiverAddress().getBytes();
        byte[] isAcceptedInBytes = ((Boolean)entryResult.isAccepted()).toString().getBytes();
        byte[] isUniqueByDateInBytes = ((Boolean)entryResult.isUniqueByDate()).toString().getBytes();
        byte[] isPassedPreBalanceCheckInBytes = ((Boolean)entryResult.isPassedPreBalanceCheck()).toString().getBytes();

        byte[] resultLineInBytes = ByteBuffer.allocate(distributionPoolNameInBytes.length + sourceInBytes.length + distributionPoolAddressInBytes.length +
                receiverAddressInBytes.length + isAcceptedInBytes.length + isUniqueByDateInBytes.length + isPassedPreBalanceCheckInBytes.length).
                put(distributionPoolNameInBytes).put(sourceInBytes).put(distributionPoolAddressInBytes).put(receiverAddressInBytes).
                put(isAcceptedInBytes).put(isUniqueByDateInBytes).put(isPassedPreBalanceCheckInBytes).array();
        fundDistributionFileResultData.getSignatureMessage().add(resultLineInBytes);
        fundDistributionFileResultData.incrementMessageByteSize(resultLineInBytes.length);
    }



}
