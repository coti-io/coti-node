package io.coti.basenode.services;

import com.google.gson.Gson;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.GetClusterStampFileNamesCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.http.GetClusterStampFileNamesResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.SerializableResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.LastClusterStampVersions;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.CLUSTERSTAMP_MAJOR_NOT_FOUND;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class BaseNodeClusterStampService implements IClusterStampService {

    private static final int CLUSTERSTAMP_NAME_ARRAY_NOT_UPDATED_LENGTH = 3;
    private static final int CLUSTERSTAMP_UPDATE_TIME_AND_FILE_TYPE_NOT_UPDATED_INDEX = 2;
    private static final int CLUSTERSTAMP_NAME_ARRAY_LENGTH = 4;
    private static final int CLUSTERSTAMP_CONST_PREFIX_INDEX = 0;
    private static final int CLUSTERSTAMP_TYPE_MARK_INDEX = 1;
    private static final int CLUSTERSTAMP_VERSION_TIME_INDEX = 2;
    private static final int CLUSTERSTAMP_UPDATE_TIME_AND_FILE_TYPE_INDEX = 3;
    private static final int CLUSTERSTAMP_VERSION_OR_UPDATE_TIME_AND_FILE_TYPE_ARRAY_LENGTH = 2;
    private static final int CLUSTERSTAMP_UPDATE_TIME_INDEX = 0;
    private static final int CLUSTERSTAMP_VERSION_TIME_NOT_UPDATED_INDEX = 0;
    private static final int CLUSTERSTAMP_FILE_TYPE_INDEX = 1;
    private static final int NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES = 1; // Genesis One and Two + heading
    private static final int DETAILS_IN_CLUSTERSTAMP_LINE_WITHOUT_CURRENCY_HASH = 2;
    private static final int DETAILS_IN_CLUSTERSTAMP_LINE_WITH_CURRENCY_HASH = 3;
    private static final int ADDRESS_HASH_INDEX_IN_CLUSTERSTAMP_LINE = 0;
    private static final int AMOUNT_INDEX_IN_CLUSTERSTAMP_LINE = 1;
    private static final int CURRENCY_HASH_INDEX_IN_CLUSTERSTAMP_LINE = 2;
    private static final int NUMBER_OF_SIGNATURE_LINE_DETAILS = 2;
    private static final int LONG_MAX_LENGTH = 19;
    protected static final String BAD_CSV_FILE_FORMAT = "Bad csv file format";
    private static final String SIGNATURE_LINE_TOKEN = "# Signature";
    private static final String CLUSTERSTAMP_FILE_PREFIX = "clusterstamp";
    private static final String CLUSTERSTAMP_FILE_TYPE = "csv";
    private static final String CLUSTERSTAMP_ENDPOINT = "/clusterstamps";
    protected static ClusterStampNameData majorClusterStampName;
    protected static Map<Hash, ClusterStampNameData> tokenClusterStampHashToName;
    @Value("${clusterstamp.folder}")
    protected String clusterStampFolder;
    protected String clusterStampBucketName;
    @Value("${application.name}")
    private String applicationName;
    @Value("${currency.genesis.address}")
    private Hash currencyGenesisAddress;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    protected TrustChainConfirmationService trustChainConfirmationService;
    @Autowired
    protected Transactions transactions;
    @Autowired
    protected ClusterStampCrypto clusterStampCrypto;
    @Autowired
    private GetClusterStampFileNamesCrypto getClusterStampFileNamesCrypto;
    @Autowired
    protected INetworkService networkService;
    @Autowired
    protected IAwsService awsService;
    @Autowired
    protected LastClusterStampVersions lastClusterStampVersions;
    @Autowired
    protected BaseNodeFileSystemService fileSystemService;
    @Autowired
    protected ICurrencyService currencyService;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected IMintingService mintingService;

    @Override
    public void init() {
        try {
            fileSystemService.createFolder(clusterStampFolder);
            initLocalClusterStampNames();
            fillClusterStampNamesMap();
            getClusterStampFromRecoveryServer(true);
            loadAllClusterStamps();
            log.info("{} is up", this.getClass().getSimpleName());
        } catch (ClusterStampException e) {
            throw new ClusterStampException("Error at clusterstamp init.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException("Error at clusterstamp init.", e);
        }
    }


    private void initLocalClusterStampNames() {
        majorClusterStampName = null;
        tokenClusterStampHashToName = new HashMap<>();
    }

    protected void fillClusterStampNamesMap() {
        List<String> clusterStampFileNames = fileSystemService.listFolderFileNames(clusterStampFolder);
        for (String clusterStampFileName : clusterStampFileNames) {
            ClusterStampNameData clusterStampNameData = validateNameAndGetClusterStampNameData(clusterStampFileName);
            if (clusterStampNameData.isMajor() && majorClusterStampName != null) {
                throw new ClusterStampException(String.format("Error, Multiple local major clusterstamps found: [%s, %s] .Please remove excess clusterstamps and restart.", majorClusterStampName, getClusterStampFileName(clusterStampNameData)));
            }
            addClusterStampName(clusterStampNameData);
        }
    }

    private ClusterStampNameData validateNameAndGetClusterStampNameData(String clusterStampFileName) {
        String[] delimitedFileName = clusterStampFileName.split("_");
        if (delimitedFileName.length != CLUSTERSTAMP_NAME_ARRAY_LENGTH && delimitedFileName.length != CLUSTERSTAMP_NAME_ARRAY_NOT_UPDATED_LENGTH) {
            throw new ClusterStampValidationException(String.format("Bad cluster stamp file name: %s. Please correct clusterstamp file name and restart.", clusterStampFileName));
        }
        String clusterStampConstantPrefix = delimitedFileName[CLUSTERSTAMP_CONST_PREFIX_INDEX];
        String clusterStampTypeMark = delimitedFileName[CLUSTERSTAMP_TYPE_MARK_INDEX];
        String clusterStampUpdateTime;
        String clusterStampVersionTime;
        String clusterStampFileType;
        if (delimitedFileName.length == CLUSTERSTAMP_NAME_ARRAY_NOT_UPDATED_LENGTH) {
            String[] delimitedClusterStampVersionTimeAndFileType = validateAndGetClusterStampNameLastDelimitedPart(clusterStampFileName, delimitedFileName[CLUSTERSTAMP_UPDATE_TIME_AND_FILE_TYPE_NOT_UPDATED_INDEX]);
            clusterStampVersionTime = delimitedClusterStampVersionTimeAndFileType[CLUSTERSTAMP_VERSION_TIME_NOT_UPDATED_INDEX];
            clusterStampUpdateTime = clusterStampVersionTime;
            clusterStampFileType = delimitedClusterStampVersionTimeAndFileType[CLUSTERSTAMP_FILE_TYPE_INDEX];
        } else {
            clusterStampVersionTime = delimitedFileName[CLUSTERSTAMP_VERSION_TIME_INDEX];
            String[] delimitedClusterStampUpdateTimeAndFileType = validateAndGetClusterStampNameLastDelimitedPart(clusterStampFileName, delimitedFileName[CLUSTERSTAMP_UPDATE_TIME_AND_FILE_TYPE_INDEX]);
            clusterStampUpdateTime = delimitedClusterStampUpdateTimeAndFileType[CLUSTERSTAMP_UPDATE_TIME_INDEX];
            clusterStampFileType = delimitedClusterStampUpdateTimeAndFileType[CLUSTERSTAMP_FILE_TYPE_INDEX];
        }
        if (!validateClusterStampFileName(clusterStampConstantPrefix, clusterStampTypeMark, clusterStampVersionTime, clusterStampUpdateTime, clusterStampFileType)) {
            throw new ClusterStampValidationException(String.format("Bad cluster stamp file name: %s. Please correct clusterstamp name and restart.", clusterStampFileName));
        }
        return new ClusterStampNameData(ClusterStampType.getTypeByMark(clusterStampTypeMark).get(), clusterStampVersionTime, clusterStampUpdateTime);
    }

    private String[] validateAndGetClusterStampNameLastDelimitedPart(String clusterStampFileName, String clusterStampNameLastPart) {
        String[] clusterStampNameLastDelimitedPart = clusterStampNameLastPart.split("\\.");
        if (clusterStampNameLastDelimitedPart.length != CLUSTERSTAMP_VERSION_OR_UPDATE_TIME_AND_FILE_TYPE_ARRAY_LENGTH) {
            throw new ClusterStampException(String.format("Bad cluster stamp file name: %s. Please correct clusterstamp name and restart.", clusterStampFileName));
        }
        return clusterStampNameLastDelimitedPart;
    }

    private boolean validateClusterStampFileName(String clusterStampConstantPrefix, String clusterStampTypeMark, String clusterStampVersionTime, String clusterStampUpdateTime, String clusterStampFileType) {
        return clusterStampConstantPrefix.equals(CLUSTERSTAMP_FILE_PREFIX)
                && ClusterStampType.getTypeByMark(clusterStampTypeMark).isPresent()
                && isLong(clusterStampVersionTime)
                && isLong(clusterStampUpdateTime)
                && Long.parseLong(clusterStampUpdateTime) >= Long.parseLong(clusterStampVersionTime)
                && clusterStampFileType.equals(CLUSTERSTAMP_FILE_TYPE);
    }

    private boolean isLong(String string) {
        return NumberUtils.isDigits(string) && string.length() <= LONG_MAX_LENGTH;
    }

    private void loadAllClusterStamps() {
        log.info("Loading clusterstamp files");
        loadClusterStamp(majorClusterStampName);
        tokenClusterStampHashToName.values().forEach(clusterStampNameData ->
                loadClusterStamp(clusterStampNameData));
    }

    protected void addClusterStampName(ClusterStampNameData clusterStampNameData) {
        if (clusterStampNameData.isMajor()) {
            majorClusterStampName = clusterStampNameData;
        } else {
            tokenClusterStampHashToName.put(clusterStampNameData.getHash(), clusterStampNameData);
        }
    }

    private void removeClusterStampName(ClusterStampNameData clusterStampNameData) {
        if (clusterStampNameData.isMajor()) {
            majorClusterStampName = null;
        } else {
            tokenClusterStampHashToName.remove(clusterStampNameData.getHash());
        }
    }

    private boolean isClusterStampNameExists(ClusterStampNameData clusterStampNameData) {
        if (clusterStampNameData.isMajor()) {
            return majorClusterStampName.equals(clusterStampNameData);
        }
        return (tokenClusterStampHashToName.get(clusterStampNameData.getHash()) != null);
    }

    protected String getClusterStampFileName(ClusterStampNameData clusterStampNameData) {
        Long versionTimeMillis = clusterStampNameData.getVersionTimeMillis();
        Long creationTimeMillis = clusterStampNameData.getCreationTimeMillis();
        StringBuilder sb = new StringBuilder(CLUSTERSTAMP_FILE_PREFIX);
        sb.append("_").append(clusterStampNameData.getType().getMark()).append("_").append(versionTimeMillis.toString());
        if (!versionTimeMillis.equals(creationTimeMillis)) {
            sb.append("_").append(creationTimeMillis.toString());
        }
        return sb.append(".").append(CLUSTERSTAMP_FILE_TYPE).toString();
    }

    protected void loadClusterStamp(ClusterStampNameData clusterStampNameData) {
        String clusterStampFileName = getClusterStampFileName(clusterStampNameData);
        log.info("Starting to load clusterstamp file {}", clusterStampFileName);
        String clusterStampFileLocation = clusterStampFolder + clusterStampFileName;
        File clusterstampFile = new File(clusterStampFileLocation);
        ClusterStampData clusterStampData = new ClusterStampData();
        Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(clusterstampFile))) {
            String line;
            AtomicInteger relevantLineNumber = new AtomicInteger(0);
            AtomicInteger signatureRelevantLines = new AtomicInteger(0);
            boolean reachedSignatureSection = false;
            boolean finishedBalances = false;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                relevantLineNumber.incrementAndGet();
                if (line.isEmpty()) {
                    if (relevantLineNumber.get() < NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES) {
                        throw new ClusterStampValidationException(String.format("Clusterstamp file %s has less than necessary number of balance lines", clusterStampFileName));
                    } else {
                        if (!finishedBalances)
                            finishedBalances = true;
                        else
                            throw new ClusterStampValidationException(String.format("Unnecessary empty line at clusterstamp file %s.", clusterStampFileName));
                    }
                } else {
                    if (!finishedBalances) {
                        fillBalanceFromLine(clusterStampData, line, clusterStampCurrencyMap, clusterStampFileName);
                    } else {
                        if (!reachedSignatureSection) {
                            if (!line.contentEquals(SIGNATURE_LINE_TOKEN))
                                throw new ClusterStampValidationException(String.format("Invalid signature line notification at clusterstamp file %s", clusterStampFileName));
                            else
                                reachedSignatureSection = true;
                        } else {
                            signatureRelevantLines.incrementAndGet();
                            fillSignatureDataFromLine(clusterStampData, line, signatureRelevantLines);
                        }

                    }
                }
            }
            if (clusterStampCurrencyMap.entrySet().stream().anyMatch(entry -> entry.getValue().getAmount().compareTo(BigDecimal.ZERO) != 0)) {
                throw new ClusterStampValidationException(String.format("Wrong currency balances at clusterstamp file %s.", clusterStampFileName));
            }
            if (signatureRelevantLines.get() == 0) {
                handleClusterStampWithoutSignature(clusterStampData, clusterStampFileLocation);
            } else if (signatureRelevantLines.get() == 1) {
                throw new ClusterStampValidationException(String.format("Signature lines can not be a single line at clusterstamp file %s", clusterStampFileName));
            } else {
                handleClusterStampWithSignature(clusterStampData);
            }
            balanceService.updatePreBalanceFromClusterStamp();
            mintingService.updateMintingBalanceFromClusterStamp(clusterStampCurrencyMap.keySet(), currencyGenesisAddress);
            log.info("Finished to load clusterstamp file {}", clusterStampFileName);
        } catch (ClusterStampException e) {
            throw new ClusterStampException(String.format("Errors on clusterstamp file %s loading.\n", clusterStampFileName) + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Errors on clusterstamp file %s loading.", clusterStampFileName), e);
        }
    }

    protected void handleMissingRecoveryServer() {
        throw new ClusterStampException("Recovery server undefined.");
    }

    @Override
    public void getClusterStampFromRecoveryServer(boolean isStartup) {
        String recoveryServerAddress = networkService.getRecoveryServerAddress();
        if (recoveryServerAddress == null) {
            handleMissingRecoveryServer();
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            GetClusterStampFileNamesResponse getClusterStampFileNamesResponse = restTemplate.getForObject(recoveryServerAddress + CLUSTERSTAMP_ENDPOINT, GetClusterStampFileNamesResponse.class);
            if (!getClusterStampFileNamesCrypto.verifySignature(getClusterStampFileNamesResponse)) {
                throw new ClusterStampException(String.format("Cluster stamp retrieval failed. Bad signature for response from recovery server %s.", recoveryServerAddress));
            }
            clusterStampBucketName = getClusterStampFileNamesResponse.getClusterStampBucketName();
            handleRequiredClusterStampFiles(getClusterStampFileNamesResponse, isStartup);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ClusterStampException(String.format("Clusterstamp recovery failed. Recovery server response: %s.", new Gson().fromJson(e.getResponseBodyAsString(), Response.class).getMessage()), e);
        } catch (ClusterStampException e) {
            throw new ClusterStampException("Clusterstamp recovery failed.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException("Clusterstamp recovery failed.", e);
        }

    }

    private void handleRequiredClusterStampFiles(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, boolean isStartup) {
        if (!validateResponseVersionValidity(getClusterStampFileNamesResponse)) {
            throw new ClusterStampValidationException("Recovery clusterstamp version is not valid");
        }
        if (majorClusterStampName == null) {
            handleMissingClusterStampsWithMajorNotPresent(getClusterStampFileNamesResponse, isStartup);
            return;
        }
        handleMissingClusterStampsWithMajorPresent(getClusterStampFileNamesResponse, isStartup);
    }

    private boolean validateResponseVersionValidity(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse) {
        LastClusterStampVersionData lastVersionData = lastClusterStampVersions.get();
        return lastVersionData == null || lastVersionData.getVersionTimeMillis() == null ||
                (validateVersion(lastVersionData.getVersionTimeMillis(), getClusterStampFileNamesResponse.getMajor().getVersionTimeMillis()) &&
                        getClusterStampFileNamesResponse.getTokenClusterStampNames().stream().
                                allMatch(clusterStampNameData -> validateVersion(lastVersionData.getVersionTimeMillis(), clusterStampNameData.getVersionTimeMillis())));
    }

    private boolean validateVersion(Long clusterStampDBVersion, Long clusterStampFileVersion) {
        return clusterStampFileVersion >= clusterStampDBVersion;
    }

    @Override
    public boolean shouldUpdateClusterStampDBVersion() {
        LastClusterStampVersionData lastVersionData = lastClusterStampVersions.get();
        return lastVersionData == null || lastVersionData.getVersionTimeMillis() == null || majorClusterStampName.getVersionTimeMillis() > lastVersionData.getVersionTimeMillis();
    }

    @Override
    public boolean isClusterStampDBVersionExist() {
        return lastClusterStampVersions.get() != null;
    }

    @Override
    public void setClusterStampDBVersion() {
        lastClusterStampVersions.put(new LastClusterStampVersionData(majorClusterStampName.getVersionTimeMillis()));
        log.info("Clusterstamp version time is set to {}", Instant.ofEpochMilli(majorClusterStampName.getVersionTimeMillis()));
    }

    private void handleMissingClusterStampsWithMajorNotPresent(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, boolean isStartup) {
        clearClusterStampNamesAndFiles();
        downloadAndAddSingleClusterStamp(getClusterStampFileNamesResponse.getMajor());
        downloadAndAddClusterStamps(getClusterStampFileNamesResponse.getTokenClusterStampNames());
        if (!isStartup) {
            loadAllClusterStamps();
        }
    }

    private void clearClusterStampNamesAndFiles() {
        try {
            tokenClusterStampHashToName = new HashMap<>();
            fileSystemService.removeFolderContents(clusterStampFolder);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Failed to remove %s folder contents. Please manually delete all clusterstamps and restart.", clusterStampFolder), e);
        }
    }

    private void handleMissingClusterStampsWithMajorPresent(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, boolean isStartup) {
        ClusterStampNameData majorFromRecovery = getClusterStampFileNamesResponse.getMajor();
        if (majorClusterStampName.equals(majorFromRecovery)) {
            handleMajorsEqual(getClusterStampFileNamesResponse, isStartup);
        } else if (majorClusterStampName.getVersionTimeMillis().equals(majorFromRecovery.getVersionTimeMillis())) {
            handleUpdatedMajor(getClusterStampFileNamesResponse, isStartup);
        } else {
            handleDifferentMajorVersions(getClusterStampFileNamesResponse, isStartup);
        }
    }

    private void handleMajorsEqual(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, boolean isStartup) {
        List<ClusterStampNameData> missingTokens = validateNoExcessTokensAndGetMissingTokens(getClusterStampFileNamesResponse);
        downloadAndAddClusterStamps(missingTokens);
        if (!isStartup) {
            missingTokens.forEach(clusterStampNameData ->
                    loadClusterStamp(clusterStampNameData));
        }
    }

    private void handleUpdatedMajor(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, boolean isStartup) {
        ClusterStampNameData majorFromRecovery = getClusterStampFileNamesResponse.getMajor();
        removeClusterStampNameAndFile(majorClusterStampName);
        List<ClusterStampNameData> missingTokens = validateNoExcessTokensAndGetMissingTokens(getClusterStampFileNamesResponse);
        downloadAndAddSingleClusterStamp(majorFromRecovery);
        downloadAndAddClusterStamps(missingTokens);
        if (!isStartup) {
            loadClusterStamp(majorClusterStampName);
            missingTokens.forEach(clusterStampNameData ->
                    loadClusterStamp(clusterStampNameData));
        }
    }

    private void handleDifferentMajorVersions(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, boolean isStartup) {
        removeClusterStampNameAndFile(majorClusterStampName);
        removeClusterStampNamesAndFiles(new ArrayList<>(tokenClusterStampHashToName.values()));
        downloadAndAddSingleClusterStamp(getClusterStampFileNamesResponse.getMajor());
        downloadAndAddClusterStamps(getClusterStampFileNamesResponse.getTokenClusterStampNames());
        if (!isStartup) {
            loadAllClusterStamps();
        }
    }

    private void removeClusterStampNamesAndFiles(List<ClusterStampNameData> localTokens) {
        localTokens.forEach(this::removeClusterStampNameAndFile);
    }

    private void removeClusterStampNameAndFile(ClusterStampNameData clusterStampNameData) {
        removeClusterStampName(clusterStampNameData);
        String clusterStampFileName = getClusterStampFileName(clusterStampNameData);
        String clusterStampFilePath = clusterStampFolder + clusterStampFileName;
        try {
            fileSystemService.deleteFile(clusterStampFilePath);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Failed to delete clusterstamp file %s. Please delete manually and restart.", clusterStampFileName), e);
        }
    }

    private List<ClusterStampNameData> validateNoExcessTokensAndGetMissingTokens(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse) {
        Map<Hash, ClusterStampNameData> localTokens = new HashMap<>(tokenClusterStampHashToName);
        List<ClusterStampNameData> missingClusterStamps = new ArrayList<>();
        getClusterStampFileNamesResponse.getTokenClusterStampNames().forEach(remoteTokenClusterStamp -> {
            if (localTokens.get(remoteTokenClusterStamp.getHash()) != null) {
                localTokens.remove(remoteTokenClusterStamp.getHash());
            } else {
                missingClusterStamps.add(remoteTokenClusterStamp);
            }
        });
        if (!localTokens.isEmpty()) {
            StringBuilder sb = new StringBuilder("Excess tokens found locally: ");
            localTokens.values().forEach(excessLocalClusterStampNameData -> sb.append(getClusterStampFileName(excessLocalClusterStampNameData) + " "));
            throw new ClusterStampValidationException(sb.toString());
        }
        return missingClusterStamps;
    }

    private void downloadAndAddSingleClusterStamp(ClusterStampNameData clusterStampNameData) {
        String clusterStampFileName = getClusterStampFileName(clusterStampNameData);
        String filePath = clusterStampFolder + clusterStampFileName;
        try {
            awsService.downloadFile(filePath, clusterStampBucketName);
            addClusterStampName(clusterStampNameData);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Couldn't download clusterstamp file %s.", clusterStampFileName), e);
        }
    }

    private void downloadAndAddClusterStamps(List<ClusterStampNameData> clusterStampsToAdd) {
        clusterStampsToAdd.forEach(this::downloadAndAddSingleClusterStamp);
    }

    @Override
    public ResponseEntity<IResponse> getRequiredClusterStampNames() {
        GetClusterStampFileNamesResponse getClusterStampFileNamesResponse = new GetClusterStampFileNamesResponse();
        if (majorClusterStampName == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SerializableResponse(CLUSTERSTAMP_MAJOR_NOT_FOUND, STATUS_ERROR));
        }
        getClusterStampFileNamesResponse.setMajor(majorClusterStampName);
        getClusterStampFileNamesResponse.setTokenClusterStampNames(getLocalTokensList());
        getClusterStampFileNamesResponse.setClusterStampBucketName(clusterStampBucketName);
        getClusterStampFileNamesCrypto.signMessage(getClusterStampFileNamesResponse);
        return ResponseEntity.ok(getClusterStampFileNamesResponse);
    }

    private List<ClusterStampNameData> getLocalTokensList() {
        return tokenClusterStampHashToName.values().stream().collect(Collectors.toList());
    }

    private void fillBalanceFromLine(ClusterStampData clusterStampData, String line, Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap, String clusterStampFileName) {
        try {
            String[] lineDetails = line.split(",");
            int numOfDetailsInLine = lineDetails.length;
            if (numOfDetailsInLine != DETAILS_IN_CLUSTERSTAMP_LINE_WITH_CURRENCY_HASH && numOfDetailsInLine != DETAILS_IN_CLUSTERSTAMP_LINE_WITHOUT_CURRENCY_HASH) {
                throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
            }
            Hash addressHash = new Hash(lineDetails[ADDRESS_HASH_INDEX_IN_CLUSTERSTAMP_LINE]);
            BigDecimal currencyAmountInAddress = new BigDecimal(lineDetails[AMOUNT_INDEX_IN_CLUSTERSTAMP_LINE]);
            Hash currencyHash = numOfDetailsInLine == DETAILS_IN_CLUSTERSTAMP_LINE_WITH_CURRENCY_HASH && !lineDetails[CURRENCY_HASH_INDEX_IN_CLUSTERSTAMP_LINE].isEmpty() ? new Hash(lineDetails[CURRENCY_HASH_INDEX_IN_CLUSTERSTAMP_LINE]) : null;
            if (currencyHash == null) {
                currencyHash = currencyService.getNativeCurrencyHash();
            }

            validateClusterStampLineDetails(currencyAmountInAddress, currencyHash, clusterStampCurrencyMap, clusterStampFileName);
            balanceService.updateBalanceFromClusterStamp(addressHash, currencyHash, currencyAmountInAddress);
            log.trace("The address hash {} for currency hash {} was loaded from the clusterstamp {} with amount {}", addressHash, currencyHash, clusterStampFileName, currencyAmountInAddress);

            byte[] addressHashInBytes = addressHash.getBytes();
            byte[] addressCurrencyAmountInBytes = currencyAmountInAddress.stripTrailingZeros().toPlainString().getBytes();
            byte[] currencyHashInBytes = numOfDetailsInLine == DETAILS_IN_CLUSTERSTAMP_LINE_WITH_CURRENCY_HASH ? currencyHash.getBytes() : new byte[0];
            byte[] balanceInBytes = ByteBuffer.allocate(addressHashInBytes.length + addressCurrencyAmountInBytes.length + currencyHashInBytes.length)
                    .put(addressHashInBytes).put(addressCurrencyAmountInBytes).put(currencyHashInBytes).array();
            clusterStampData.getSignatureMessage().add(balanceInBytes);
            clusterStampData.incrementMessageByteSize(balanceInBytes.length);
        } catch (ClusterStampException e) {
            throw new ClusterStampException(String.format("Error at filling balance from line of clusterstamp %s.\n", clusterStampFileName) + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Error at filling balance from line of clusterstamp %s.", clusterStampFileName), e);
        }
    }

    private void validateClusterStampLineDetails(BigDecimal currencyAmountInAddress, Hash
            currencyHash, Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap, String clusterStampFileName) {
        ClusterStampCurrencyData clusterStampCurrencyData = clusterStampCurrencyMap.get(currencyHash);
        if (clusterStampCurrencyData == null) {
            CurrencyData currencyData = currencyService.getCurrencyFromDB(currencyHash);
            if (currencyData == null) {
                throw new ClusterStampValidationException(String.format("Currency %s in clusterstamp file %s not found at DB", currencyHash, clusterStampFileName));
            }
            clusterStampCurrencyData = new ClusterStampCurrencyData(currencyData);
            clusterStampCurrencyMap.put(currencyHash, clusterStampCurrencyData);
        }

        int scale = clusterStampCurrencyData.getScale();
        if (currencyAmountInAddress.scale() > scale) {
            throw new ClusterStampValidationException(String.format("Scale of currency %s in clusterstamp file is wrong for amount %s.", currencyHash, currencyAmountInAddress));
        }

        BigDecimal subtractedCurrencyAmount = clusterStampCurrencyData.getAmount().subtract(currencyAmountInAddress);
        if (subtractedCurrencyAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ClusterStampValidationException(String.format("Total amount of currency %s in clusterstamp file exceeds currency supply.", currencyHash));
        }
        clusterStampCurrencyData.setAmount(subtractedCurrencyAmount);
    }

    private void fillSignatureDataFromLine(ClusterStampData clusterStampData, String line, int signatureRelevantLines) {
        if (signatureRelevantLines.get() > 2) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }

        String[] signatureDetails;
        signatureDetails = line.split(",");
        if (signatureDetails.length != NUMBER_OF_SIGNATURE_LINE_DETAILS) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }
        String signaturePrefix = (signatureRelevantLines.get() == 1) ? "r" : "s";
        if (!signatureDetails[0].equalsIgnoreCase(signaturePrefix)) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }

        if (signatureRelevantLines.get() == 1) {
            SignatureData signature = new SignatureData();
            clusterStampData.setSignature(signature);
            clusterStampData.getSignature().setR(signatureDetails[1]);
        } else
            clusterStampData.getSignature().setS(signatureDetails[1]);
    }

    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData, String
            clusterStampFileLocation) {
        throw new ClusterStampValidationException(String.format("Clusterstamp file has no signature."));
    }

    private void handleClusterStampWithSignature(ClusterStampData clusterStampData) {
        setClusterStampSignerHash(clusterStampData);
        if (!clusterStampCrypto.verifySignature(clusterStampData)) {
            throw new ClusterStampValidationException("Invalid signature.");
        }
    }

    protected void setClusterStampSignerHash(ClusterStampData clusterStampData) {
        clusterStampData.setSignerHash(networkService.getSingleNodeData(NodeType.ZeroSpendServer).getNodeHash());
    }

    @Override
    public void handleInitiatedTokenNotice(InitiatedTokenNoticeData initiatedTokenNoticeData) {
        if (!isClusterStampNameExists(initiatedTokenNoticeData.getClusterStampNameData())) {
            downloadAndAddSingleClusterStamp(initiatedTokenNoticeData.getClusterStampNameData());
            loadClusterStamp(initiatedTokenNoticeData.getClusterStampNameData());
        }
    }

}