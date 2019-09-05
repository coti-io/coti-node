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
import io.coti.basenode.services.interfaces.IAwsService;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
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

    private static final int CLUSTERSTAMP_NAME_ARRAY_LENGTH = 4;
    private static final int CLUSTERSTAMP_CONST_PREFIX_INDEX = 0;
    private static final int CLUSTERSTAMP_TYPE_MARK_INDEX = 1;
    private static final int CLUSTERSTAMP_VERSION_TIME_INDEX = 2;
    private static final int CLUSTERSTAMP_UPDATE_TIME_AND_FILE_TYPE_INDEX = 3;
    private static final int CLUSTERSTAMP_UPDATE_TIME_AND_FILE_TYPE_ARRAY_LENGTH = 2;
    private static final int CLUSTERSTAMP_UPDATE_TIME_INDEX = 0;
    private static final int CLUSTERSTAMP_FILE_TYPE_INDEX = 1;
    private static final int NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES = 1; // Genesis One and Two + heading
    private static final int NUMBER_OF_ADDRESS_LINE_DETAILS = 2;
    private static final int ADDRESS_DETAILS_HASH_INDEX = 0;
    private static final int ADDRESS_DETAILS_AMOUNT_INDEX = 1;
    private static final int NUMBER_OF_SIGNATURE_LINE_DETAILS = 2;
    private static final int LONG_MAX_LENGTH = 19;
    protected static final String BAD_CSV_FILE_FORMAT = "Bad csv file format";
    private static final String SIGNATURE_LINE_TOKEN = "# Signature";
    private static final String CLUSTERSTAMP_FILE_PREFIX = "Clusterstamp";
    private static final String CLUSTERSTAMP_FILE_TYPE = "csv";
    private static final String CLUSTERSTAMP_ENDPOINT = "/clusterstamps";

    protected static ClusterStampNameData majorClusterStampName;
    protected static Map<Hash, ClusterStampNameData> tokenClusterStampHashToName;
    @Value("${clusterstamp.folder}")
    private String clusterStampsFolder;
    @Value("${aws.s3.bucket.name.clusterstamp}")
    private String clusterStampBucketName;
    @Value("${application.name}")
    private String applicationName;
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
    private IAwsService awsService;
    @Autowired
    private LastClusterStampVersions lastClusterStampVersions;
    @Autowired
    private BaseNodeFileSystemService fileSystemService;

    @Override
    public void init() {
        try {
            fileSystemService.createFolder(clusterStampsFolder);
            initLocalClusterStampNames();
            fillClusterStampNamesMap();
            getClusterStampFromRecoveryServer(true);
            loadAllClusterStamps();
        } catch (ClusterStampException e) {
            throw new ClusterStampException("Error at clusterstamp init. " + e.getMessage());
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Error at clusterstamp init. Exception: %s, exception error: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    private void initLocalClusterStampNames() {
        majorClusterStampName = null;
        tokenClusterStampHashToName = new HashMap<>();
    }

    private void fillClusterStampNamesMap() {
        List<String> clusterStampFileNames = fileSystemService.listFolderFileNames(clusterStampsFolder);
        for (String clusterStampFileName : clusterStampFileNames) {
            ClusterStampNameData clusterStampNameData = validateNameAndGetClusterStampNameData(clusterStampFileName);
            if (clusterStampNameData.isMajor() && majorClusterStampName != null) {
                throw new ClusterStampException(String.format("Error, Multiple local major clusterstamps found: [%s, %s] .Please remove excess clusterstamps and restart.", majorClusterStampName, getClusterStampFileName(clusterStampNameData)));
            }
            addClusterStampName(clusterStampNameData);
        }
        if (majorClusterStampName == null) {
            handleMissingMajor();
        }
    }

    protected void handleMissingMajor() {
        // Handled differently per node. base case is to do nothing.
    }

    private ClusterStampNameData validateNameAndGetClusterStampNameData(String clusterStampFileName) {
        String[] delimitedFileName = clusterStampFileName.split("_");
        if (delimitedFileName.length != CLUSTERSTAMP_NAME_ARRAY_LENGTH) {
            throw new ClusterStampException(String.format("Bad cluster stamp file name: %s. Please correct clusterstamp file name and restart.", clusterStampFileName));
        }

        String clusterStampConstantPrefix = delimitedFileName[CLUSTERSTAMP_CONST_PREFIX_INDEX];
        String clusterStampTypeMark = delimitedFileName[CLUSTERSTAMP_TYPE_MARK_INDEX];
        String clusterStampVersionTime = delimitedFileName[CLUSTERSTAMP_VERSION_TIME_INDEX];
        String[] delimitedClusterStampUpdateTimeAndFileType = delimitedFileName[CLUSTERSTAMP_UPDATE_TIME_AND_FILE_TYPE_INDEX].split("\\.");
        if (delimitedClusterStampUpdateTimeAndFileType.length != CLUSTERSTAMP_UPDATE_TIME_AND_FILE_TYPE_ARRAY_LENGTH) {
            throw new ClusterStampException(String.format("Bad cluster stamp file name: %s. Please correct clusterstamp name and restart.", clusterStampFileName));
        }

        String clusterStampUpdateTime = delimitedClusterStampUpdateTimeAndFileType[CLUSTERSTAMP_UPDATE_TIME_INDEX];
        String clusterStampFileType = delimitedClusterStampUpdateTimeAndFileType[CLUSTERSTAMP_FILE_TYPE_INDEX];
        if (!validateClusterStampFileName(clusterStampConstantPrefix, clusterStampTypeMark, clusterStampVersionTime, clusterStampUpdateTime, clusterStampFileType)) {
            throw new ClusterStampException(String.format("Bad cluster stamp file name: %s. Please correct clusterstamp name and restart.", clusterStampFileName));
        }
        return new ClusterStampNameData(ClusterStampType.getTypeByMark(clusterStampTypeMark).get(), clusterStampVersionTime, clusterStampUpdateTime);
    }

    private boolean validateClusterStampFileName(String clusterStampConstantPrefix, String clusterStampTypeMark, String clusterStampVersionTime, String clusterStampUpdateTime, String clusterStampFileType) {
        return clusterStampConstantPrefix.equals(CLUSTERSTAMP_FILE_PREFIX)
                && ClusterStampType.getTypeByMark(clusterStampTypeMark).isPresent()
                && isLong(clusterStampVersionTime)
                && isLong(clusterStampUpdateTime)
                && clusterStampFileType.equals(CLUSTERSTAMP_FILE_TYPE);
    }

    private boolean isLong(String string) {
        return NumberUtils.isDigits(string) && string.length() <= LONG_MAX_LENGTH;
    }

    private void loadAllClusterStamps() {
        loadClusterStamp(clusterStampsFolder, majorClusterStampName);
        tokenClusterStampHashToName.values().forEach(clusterStampNameData ->
                loadClusterStamp(clusterStampsFolder, clusterStampNameData));
    }

    private void addClusterStampName(ClusterStampNameData clusterStampNameData) {
        if (clusterStampNameData.isMajor()) {
            majorClusterStampName = clusterStampNameData;
        } else {
            tokenClusterStampHashToName.put(clusterStampNameData.getHash(), clusterStampNameData);
        }
    }

    private void removeClusterStampName(ClusterStampNameData clusterStampNameData) {
        if (clusterStampNameData.isMajor()) {
            lastClusterStampVersions.deleteAll();
            majorClusterStampName = null;
        } else {
            tokenClusterStampHashToName.remove(clusterStampNameData.getHash());
        }
    }

    private String getClusterStampFileName(ClusterStampNameData clusterStampNameData) {
        Long versionTimeMillis = clusterStampNameData.getVersionTimeMillis();
        Long creationTimeMillis = clusterStampNameData.getCreationTimeMillis();
        StringBuilder sb = new StringBuilder(CLUSTERSTAMP_FILE_PREFIX);
        sb.append("_").append(clusterStampNameData.getType().getMark()).append("_").append(versionTimeMillis.toString());
        if (!versionTimeMillis.equals(creationTimeMillis)) {
            sb.append("_").append(creationTimeMillis.toString());
        }
        return sb.append(".").append(CLUSTERSTAMP_FILE_TYPE).toString();
    }

    private void loadClusterStamp(String folder, ClusterStampNameData clusterStampNameData) {
        String clusterStampFileLocation = folder + getClusterStampFileName(clusterStampNameData);
        File clusterstampFile = new File(clusterStampFileLocation);
        ClusterStampData clusterStampData = new ClusterStampData();

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
                        throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
                    } else {
                        if (!finishedBalances)
                            finishedBalances = true;
                        else
                            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
                    }
                } else {
                    if (!finishedBalances) {
                        fillBalanceFromLine(clusterStampData, line);
                    } else {
                        if (!reachedSignatureSection) {
                            if (!line.contentEquals(SIGNATURE_LINE_TOKEN))
                                throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
                            else
                                reachedSignatureSection = true;
                        } else {
                            signatureRelevantLines.incrementAndGet();
                            fillSignatureDataFromLine(clusterStampData, line, signatureRelevantLines);
                        }

                    }
                }
            }
            if (signatureRelevantLines.get() == 0) {
                handleClusterStampWithoutSignature(clusterStampData, clusterStampFileLocation);
            } else if (signatureRelevantLines.get() == 1) {
                throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
            } else {
                handleClusterStampWithSignature(clusterStampData);
            }
            balanceService.updatePreBalanceFromClusterStamp();
        } catch (Exception e) {
            log.error("Errors on clusterstamp loading");
            throw new ClusterStampValidationException(e.getMessage());
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
            handleRequiredClusterStampFiles(getClusterStampFileNamesResponse, isStartup);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ClusterStampException(String.format("Clusterstamp recovery failed. %s: %s", e.getClass().getName(), new Gson().fromJson(e.getResponseBodyAsString(), Response.class).getMessage()));
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Clusterstamp recovery failed. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    private void handleRequiredClusterStampFiles(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, boolean isStartup) {
        if(!validateResponseVersionValidity(getClusterStampFileNamesResponse)){
            throw new ClusterStampException("Recovery clusterstamp version is not valid");
        }
        if (majorClusterStampName == null) {
            handleMissingClusterStampsWithMajorNotPresent(getClusterStampFileNamesResponse, isStartup);
            return;
        }
        handleMissingClusterStampsWithMajorPresent(getClusterStampFileNamesResponse, isStartup);
    }

    private boolean validateResponseVersionValidity(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse) {
        LastClusterStampVersionData lastVersionData = lastClusterStampVersions.get();
        if(!validateVersion(lastVersionData.getVersionTimeMillis(), getClusterStampFileNamesResponse.getMajor().getVersionTimeMillis())){
            return false;
        }
        if(getClusterStampFileNamesResponse.getTokenClusterStampNames().stream().anyMatch(clusterStampNameData ->  !validateVersion(lastVersionData.getVersionTimeMillis(), clusterStampNameData.getVersionTimeMillis()))){
            return false;
        }
        return true;
    }


    private boolean validateVersion(Long currentVersion, Long clusterStampFileVersion) {
        return clusterStampFileVersion >= currentVersion;
    }


    private void  handleMissingClusterStampsWithMajorNotPresent(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, boolean isStartup){
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
            fileSystemService.removeFolderContents(clusterStampsFolder);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Failed to remove %s folder contents. Please manually delete all clusterstamps and restart. Error: %s", clusterStampsFolder, e.getMessage()));
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
                    loadClusterStamp(clusterStampsFolder, clusterStampNameData));
        }
    }

    private void handleUpdatedMajor(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, boolean isStartup) {
        ClusterStampNameData majorFromRecovery = getClusterStampFileNamesResponse.getMajor();
        removeClusterStampNameAndFile(majorClusterStampName);
        List<ClusterStampNameData> missingTokens = validateNoExcessTokensAndGetMissingTokens(getClusterStampFileNamesResponse);
        downloadAndAddSingleClusterStamp(majorFromRecovery);
        downloadAndAddClusterStamps(missingTokens);
        if (!isStartup) {
            loadClusterStamp(clusterStampsFolder, majorClusterStampName);
            missingTokens.forEach(clusterStampNameData ->
                    loadClusterStamp(clusterStampsFolder, clusterStampNameData));
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
        String clusterStampFilePath = clusterStampsFolder + getClusterStampFileName(clusterStampNameData);
        try {
            fileSystemService.deleteFile(clusterStampFilePath);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Failed to delete file %s. Please delete manually and restart. Error: %s", clusterStampFilePath, e.getMessage()));
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
            throw new ClusterStampException(sb.toString());
        }
        return missingClusterStamps;
    }

    private void downloadAndAddSingleClusterStamp(ClusterStampNameData clusterStampNameData) {
        String clusterStampFileName = getClusterStampFileName(clusterStampNameData);
        String filePath = clusterStampsFolder + clusterStampFileName;
        try {
            addClusterStampName(clusterStampNameData);
            awsService.downloadFile(filePath, clusterStampBucketName);
        } catch (IOException e) {
            throw new ClusterStampException(String.format("Couldn't download %s clusterstamp file.", clusterStampFileName, e.getMessage()));
        }
    }

    private void downloadAndAddClusterStamps(List<ClusterStampNameData> clusterStampsToAdd) {
        clusterStampsToAdd.forEach(this::downloadAndAddSingleClusterStamp);
    }

    @Override
    public ResponseEntity<IResponse> getRequiredClusterStampNames() {
        GetClusterStampFileNamesResponse getClusterStampFileNamesResponse = new GetClusterStampFileNamesResponse();
        if (majorClusterStampName == null) {
            log.error(CLUSTERSTAMP_MAJOR_NOT_FOUND);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SerializableResponse(CLUSTERSTAMP_MAJOR_NOT_FOUND, STATUS_ERROR));
        }
        getClusterStampFileNamesResponse.setMajor(majorClusterStampName);
        getClusterStampFileNamesResponse.setTokenClusterStampNames(getLocalTokensList());
        getClusterStampFileNamesCrypto.signMessage(getClusterStampFileNamesResponse);
        return ResponseEntity.ok(getClusterStampFileNamesResponse);
    }

    private List<ClusterStampNameData> getLocalTokensList() {
        return tokenClusterStampHashToName.values().stream().collect(Collectors.toList());
    }

    private void fillBalanceFromLine(ClusterStampData clusterStampData, String line) {
        String[] addressDetails;
        addressDetails = line.split(",");
        if (addressDetails.length != NUMBER_OF_ADDRESS_LINE_DETAILS) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }
        Hash addressHash = new Hash(addressDetails[ADDRESS_DETAILS_HASH_INDEX]);
        BigDecimal addressAmount = new BigDecimal(addressDetails[ADDRESS_DETAILS_AMOUNT_INDEX]);
        log.trace("The hash {} was loaded from the clusterstamp with amount {}", addressHash, addressAmount);

        balanceService.updateBalanceFromClusterStamp(addressHash, addressAmount);
        byte[] addressHashInBytes = addressHash.getBytes();
        byte[] addressAmountInBytes = addressAmount.stripTrailingZeros().toPlainString().getBytes();
        byte[] balanceInBytes = ByteBuffer.allocate(addressHashInBytes.length + addressAmountInBytes.length).put(addressHashInBytes).put(addressAmountInBytes).array();
        clusterStampData.getSignatureMessage().add(balanceInBytes);
        clusterStampData.incrementMessageByteSize(balanceInBytes.length);
    }

    private void fillSignatureDataFromLine(ClusterStampData clusterStampData, String line, AtomicInteger signatureRelevantLines) {
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

    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData, String clusterstampFileLocation) {
        throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
    }

    private void handleClusterStampWithSignature(ClusterStampData clusterStampData) {
        setClusterStampSignerHash(clusterStampData);
        if (!clusterStampCrypto.verifySignature(clusterStampData)) {
            log.error("Clusterstamp invalid signature");
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }
    }

    protected void setClusterStampSignerHash(ClusterStampData clusterStampData) {
        clusterStampData.setSignerHash(networkService.getSingleNodeData(NodeType.ZeroSpendServer).getNodeHash());
    }

}