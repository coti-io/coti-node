package io.coti.basenode.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.GetClusterStampFileNamesCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.http.GetClusterStampFileNamesResponse;
import io.coti.basenode.model.LastClusterStampVersions;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IAwsService;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.CLUSTERSTAMP_MAJOR_NOT_FOUND;

@Slf4j
@Service
public class BaseNodeClusterStampService implements IClusterStampService {

    protected static final String CLUSTERSTAMP_FILE_SUFFIX = "_clusterstamp.csv";
    private static final int NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES = 1; // Genesis One and Two + heading
    private static final int NUMBER_OF_ADDRESS_LINE_DETAILS = 2;
    private static final int ADDRESS_DETAILS_HASH_PLACEMENT = 0;
    private static final int ADDRESS_DETAILS_AMOUNT_PLACEMENT = 1;
    protected static final String BAD_CSV_FILE_FORMAT = "Bad csv file format";
    private static final String SIGNATURE_LINE_TOKEN = "# Signature";
    private static final int NUMBER_OF_SIGNATURE_LINE_DETAILS = 2;
    private static Map<ClusterStampType, Map<Hash, ClusterStampNameData>> clusterStampTypeToHashToName;
    private static String clusterStampsPath;
    @Value("${logging.file.name}")
    protected String clusterStampFilePrefix;
    @Value("${clusterstamp.folder}")
    private String clusterStampFolderPostfix;
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
    private ApplicationContext applicationContext;
    @Autowired
    private LastClusterStampVersions lastClusterStampVersions;
    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;

    @Override
    public void init() {
        clusterStampsPath = applicationName + clusterStampFolderPostfix;
        createClusterStampFolder();
        fillClusterStampNamesMap();
        getClusterStampFromRecoveryServer(true);
        loadAllClusterStamps();
    }

    private void createClusterStampFolder() {
        File clusterStampFolder = new File(clusterStampsPath);
        if (!clusterStampFolder.exists()) {
            clusterStampFolder.mkdir();
        }
    }

    private void fillClusterStampNamesMap() {
        initClusterStampNamesMap();
        File folder = new File(clusterStampsPath);
        File[] listOfClusterStampFiles = folder.listFiles();
        boolean duplicateMajor = false;
        for(File clusterStampFile : listOfClusterStampFiles){
            String clusterStampFileName = clusterStampFile.getName();
            if (validateClusterStampFileName(clusterStampFileName)) {
                ClusterStampNameData clusterStampNameData = new ClusterStampNameData(clusterStampFileName);
                if (clusterStampNameData.getType() == ClusterStampType.MAJOR && validateMajorExistence()) {
                    log.error("Error, Multiple local major clusterstamps found. Removing all previous clusterstamps.");
                    duplicateMajor = true;
                    break;
                }
                addClusterStampName(clusterStampNameData);
            } else {
                log.info("Bad cluster stamp file name: {}. File skipped", clusterStampFileName);
            }
        }
        if(duplicateMajor){
            clearClusterStampNamesCollectionAndFiles();
            LastClusterStampVersionData lastClusterStampVersionData = lastClusterStampVersions.get();
            if(lastClusterStampVersionData != null){
                lastClusterStampVersions.delete(lastClusterStampVersionData);
            }
        }
    }

    private boolean validateClusterStampFileName(String clusterStampFileName) {
        String[] delimitedFileName = clusterStampFileName.split("_");
        if (delimitedFileName.length != 4 || !delimitedFileName[0].equals("Clusterstamp") ||
                !(delimitedFileName[1].equals(ClusterStampType.MAJOR.getMark()) ||
                        delimitedFileName[1].equals(ClusterStampType.TOKEN.getMark()))) {
            return false;
        }
        String[] lastDelimited = delimitedFileName[3].split("\\.");
        return !(lastDelimited.length != 2 || !NumberUtils.isDigits(delimitedFileName[2]) || !NumberUtils.isDigits(lastDelimited[0]) || !lastDelimited[1].equals("csv"));
    }

    private void loadAllClusterStamps() {
        loadClusterStamp(clusterStampsPath + clusterStampTypeToHashToName.get(ClusterStampType.MAJOR).values().iterator().next().getClusterStampFileName());
        clusterStampTypeToHashToName.get(ClusterStampType.TOKEN).values().forEach(clusterStampNameData ->
                loadClusterStamp(clusterStampsPath + clusterStampNameData.getClusterStampFileName()));
    }

    private boolean validateMajorExistence() {
        return !clusterStampTypeToHashToName.get(ClusterStampType.MAJOR).isEmpty();
    }

    private void addClusterStampName(ClusterStampNameData clusterStampNameData) {
        if (clusterStampNameData.isMajor()) {
            lastClusterStampVersions.replacePreviousVersion(new LastClusterStampVersionData(clusterStampNameData.getVersionTime(), clusterStampNameData.getHash()));
        }
        clusterStampTypeToHashToName.get(clusterStampNameData.getType()).put(clusterStampNameData.getHash(), clusterStampNameData);
    }

    private void removeClusterStampName(ClusterStampNameData clusterStampNameData) {
        clusterStampTypeToHashToName.get(clusterStampNameData.getType()).remove(clusterStampNameData.getHash());
    }

    private void initClusterStampNamesMap() {
        clusterStampTypeToHashToName = new EnumMap<>(ClusterStampType.class);
        Arrays.stream(ClusterStampType.values()).forEach(type -> clusterStampTypeToHashToName.put(type, new HashMap<Hash, ClusterStampNameData>()));
    }

    private void loadClusterStamp(String clusterStampFileLocation) {
        File clusterstampFile = new File(clusterStampFileLocation);
        ClusterStampData clusterStampData = new ClusterStampData();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(clusterstampFile))) {
            String line;
            int relevantLineNumber = 0;
            int signatureRelevantLines = 0;
            boolean reachedSignatureSection = false;
            boolean finishedBalances = false;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                relevantLineNumber++;
                if (line.isEmpty()) {
                    if (relevantLineNumber < NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES) {
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
                            signatureRelevantLines++;
                            fillSignatureDataFromLine(clusterStampData, line, signatureRelevantLines);
                        }

                    }
                }
            }
            if (signatureRelevantLines == 0) {
                handleClusterStampWithoutSignature(clusterStampData);
            } else if (signatureRelevantLines == 1) {
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

    @Override
    public void getClusterStampFromRecoveryServer(boolean isStartup) {
        String recoveryServerAddress = networkService.getRecoveryServerAddress();
        if (recoveryServerAddress == null) {
            if (!applicationName.equals("ZeroSpend")) {
                log.error("Recovery server undefined");
            }
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<GetClusterStampFileNamesResponse> requiredClusterStampNamesResponse = restTemplate.getForEntity(recoveryServerAddress + "/clusterstamps", GetClusterStampFileNamesResponse.class);
            if (requiredClusterStampNamesResponse.getStatusCode().is2xxSuccessful()) {
                GetClusterStampFileNamesResponse getClusterStampFileNamesResponse = requiredClusterStampNamesResponse.getBody();
                if (!getClusterStampFileNamesCrypto.verifySignature(getClusterStampFileNamesResponse)) {
                    log.error("Cluster stamp retrieval failed! Bad signature for {} response.", getClusterStampFileNamesCrypto.getClass());
                    return;
                }
                if(getClusterStampFileNamesResponse.getMajor() == null){
                    log.error("Bad response from recovery server. missing major clusterstamp");
                    return;
                }
                removeExcessTokens(getClusterStampFileNamesResponse.getTokenClusterStampNames());
                handleRequiredClusterStampFiles(getClusterStampFileNamesResponse, isStartup);
                if (!isStartup) {
                    baseNodeInitializationService.initTransactionSync();
                }
            }
        } catch (Exception e) {
            log.error("Clusterstamp recovery failed. Exception: {}, Error: {}", e.getClass().getName(), e.getMessage());
        }
    }

    //TODO 8/27/2019 astolia: wrap with try catch?
    private void handleRequiredClusterStampFiles(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, boolean isStartup) {
        Optional<ClusterStampNameData> localMajorOpt = getLastKnownMajor();
        if (!localMajorOpt.isPresent()) {
            clearClusterStampNamesCollectionAndFiles();
            downloadAndAddSingleClusterStamp(getClusterStampFileNamesResponse.getMajor());
            downloadAndAddClusterStamps(getClusterStampFileNamesResponse.getTokenClusterStampNames());
            if (!isStartup) {
                loadClusterStamp(clusterStampsPath + getClusterStampFileNamesResponse.getMajor().getClusterStampFileName());
                getClusterStampFileNamesResponse.getTokenClusterStampNames().forEach(clusterStampNameData ->
                        loadClusterStamp(clusterStampsPath + clusterStampNameData.getClusterStampFileName()));
            }
            return;
        }
        handleMissingClusterStampsWithMajorPresent(getClusterStampFileNamesResponse, localMajorOpt.get(), clusterStampTypeToHashToName.get((ClusterStampType.TOKEN)), isStartup);
    }

    private void clearClusterStampNamesCollectionAndFiles() {
        initClusterStampNamesMap();
        try {
            FileUtils.cleanDirectory(new File(clusterStampsPath));
        } catch (IOException e) {
            log.error("Failed deleting clusterstamp files. Please delete all clusterstamps and restart. Exception: {}, Error: {}", e.getClass().getName(), e.getMessage());
            System.exit(SpringApplication.exit(applicationContext));
        }
    }

    //TODO 8/15/2019 astolia: handle all downloaded cluster stamp files. also delete old files if needed
    private void handleMissingClusterStampsWithMajorPresent(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, ClusterStampNameData localMajor, Map<Hash, ClusterStampNameData> localTokens, boolean isStartup) {
        ClusterStampNameData majorFromRecovery = getClusterStampFileNamesResponse.getMajor();
        //majors equal. just add missing tokens. check if i have token that should be
        if (localMajor.equals(majorFromRecovery)) {
            List<ClusterStampNameData> missingTokens = filterAndHandleMissingTokens(getClusterStampFileNamesResponse, localTokens);
            if (!isStartup) {
                missingTokens.forEach(clusterStampNameData ->
                        loadClusterStamp(clusterStampsPath + clusterStampNameData.getClusterStampFileName()));
            }
        }
        //majors aren't equal. same version but update time changes (Major was updated)
        else if (localMajor.getVersionTime().equals(majorFromRecovery.getVersionTime())) {
            removeClusterStampNameAndFile(localMajor);
            List<ClusterStampNameData> missingTokens = filterAndHandleMissingTokens(getClusterStampFileNamesResponse, localTokens);
            downloadAndAddSingleClusterStamp(majorFromRecovery);
            if (!isStartup) {
                loadClusterStamp(clusterStampsPath + majorFromRecovery.getClusterStampFileName());
                missingTokens.forEach(clusterStampNameData ->
                        loadClusterStamp(clusterStampsPath + clusterStampNameData.getClusterStampFileName()));
            }
        } else {
            // if the version is different - remove all tokens and add everything new.
            removeClusterStampNameAndFile(localMajor);
            removeClusterStampNamesAndFiles(new ArrayList<>(localTokens.values()));
            initClusterStampNamesMap();
            downloadAndAddSingleClusterStamp(majorFromRecovery);
            downloadAndAddClusterStamps(getClusterStampFileNamesResponse.getTokenClusterStampNames());
            if (!isStartup) {
                loadAllClusterStamps();
            }
        }
    }

    private void removeClusterStampNamesAndFiles(List<ClusterStampNameData> localTokens) {
        localTokens.forEach(this::removeClusterStampNameAndFile);
    }

    private void removeClusterStampNameAndFile(ClusterStampNameData clusterStampNameData) {
        removeClusterStampName(clusterStampNameData);
        try {
            Files.delete(Paths.get(clusterStampsPath + clusterStampNameData.getClusterStampFileName()));
        } catch (IOException e) {
            log.info("Failed to delete file {}. Please delete manually and restart. Error: {}", clusterStampsPath + clusterStampNameData.getClusterStampFileName(), e.getMessage());
            System.exit(SpringApplication.exit(applicationContext));
        }
    }

    private List<ClusterStampNameData> filterAndHandleMissingTokens(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, Map<Hash, ClusterStampNameData> localTokens) {
        removeExcessTokens(getClusterStampFileNamesResponse.getTokenClusterStampNames());
        List<ClusterStampNameData> missingClusterStamps = new ArrayList<>();
        getClusterStampFileNamesResponse.getTokenClusterStampNames().forEach(tokenClusterStamp -> {
            if (localTokens.get(tokenClusterStamp.getHash()) == null) {
                missingClusterStamps.add(tokenClusterStamp);
                downloadAndAddSingleClusterStamp(tokenClusterStamp);
            }
        });
        return missingClusterStamps;
    }

    private void removeExcessTokens(List<ClusterStampNameData> remoteTokenClusterStampNames) {
        Map<Hash, ClusterStampNameData> remoteTokenHashToToken = new HashMap<>();
        List<ClusterStampNameData> tokensToRemove = new ArrayList<>();
        remoteTokenClusterStampNames.forEach(clusterStampNameData -> remoteTokenHashToToken.put(clusterStampNameData.getHash(), clusterStampNameData));
        clusterStampTypeToHashToName.get(ClusterStampType.TOKEN).values().forEach(clusterStampNameData -> {
            if (remoteTokenHashToToken.get(clusterStampNameData.getHash()) == null) {
                log.error("Removing excess token clusterstamp file {}", clusterStampNameData.getClusterStampFileName());
                tokensToRemove.add(clusterStampNameData);
            }
        });
        tokensToRemove.forEach(this::removeClusterStampNameAndFile);
    }

    private void downloadAndAddSingleClusterStamp(ClusterStampNameData clusterStampNameData) {
        try {
            addClusterStampName(clusterStampNameData);
            String pathAndFileName = clusterStampsPath + clusterStampNameData.getClusterStampFileName();
            File clusterStampFile = new File(pathAndFileName);
            if (!clusterStampFile.createNewFile()) {
                log.error("Failed to create {} file.", clusterStampNameData.getClusterStampFileName());
                //TODO 9/1/2019 astolia: kill the server in case of failure?
            }
            awsService.downloadFile(pathAndFileName, clusterStampBucketName);
        } catch (IOException e) {
            log.error("Couldn't download {} clusterstamp file.", clusterStampNameData.getClusterStampFileName(), e.getMessage());
            //TODO 9/1/2019 astolia: kill the server in case of failure?
        }
    }

    private void downloadAndAddClusterStamps(List<ClusterStampNameData> clusterStamps) {
        clusterStamps.forEach(clusterStampNameData -> downloadAndAddSingleClusterStamp(clusterStampNameData));
    }

    @Override
    public ResponseEntity<GetClusterStampFileNamesResponse> getRequiredClusterStampNames() {
        GetClusterStampFileNamesResponse getClusterStampFileNamesResponse = new GetClusterStampFileNamesResponse();
        Optional<ClusterStampNameData> localMajor = getLastKnownMajor();
        if (!localMajor.isPresent()) {
            log.error("Major cluster stamp file not found.");
            handleRecoveryMissingClusterStamp();
            getClusterStampFileNamesResponse.setMajor(null);
            getClusterStampFileNamesResponse.setTokenClusterStampNames(new ArrayList<>());
            getClusterStampFileNamesResponse.setStatus(CLUSTERSTAMP_MAJOR_NOT_FOUND);
            getClusterStampFileNamesCrypto.signMessage(getClusterStampFileNamesResponse);
            return ResponseEntity.ok(getClusterStampFileNamesResponse);
        }
        getClusterStampFileNamesResponse.setMajor(localMajor.get());
        getClusterStampFileNamesResponse.setTokenClusterStampNames(extractTokens());
        getClusterStampFileNamesCrypto.signMessage(getClusterStampFileNamesResponse);
        return ResponseEntity.ok(getClusterStampFileNamesResponse);
    }

    private Optional<ClusterStampNameData> getLastKnownMajor() {
        Map<Hash, ClusterStampNameData> hashToMajor = clusterStampTypeToHashToName.get(ClusterStampType.MAJOR);
        if (hashToMajor.size() == 0) {
            return Optional.empty();
        } else if (hashToMajor.size() == 1) {
            return Optional.of(hashToMajor.entrySet().iterator().next().getValue());
        } else {
            log.error("Multiple major cluster stamps found. Removing all files.");
            handleExistingMultipleMajorClusterStamps();
            return Optional.empty();
        }
    }

    private List<ClusterStampNameData> extractTokens() {
        return clusterStampTypeToHashToName.get(ClusterStampType.TOKEN).values().stream().collect(Collectors.toList());
    }

    private void handleExistingMultipleMajorClusterStamps() {
        clearClusterStampNamesCollectionAndFiles();
    }

    private void handleRecoveryMissingClusterStamp() {
        //TODO 8/27/2019 astolia: implement
        // To be handled in the future.
        //getClusterStampFromRecoveryServer();
    }

    private void fillBalanceFromLine(ClusterStampData clusterStampData, String line) {
        String[] addressDetails;
        addressDetails = line.split(",");
        if (addressDetails.length != NUMBER_OF_ADDRESS_LINE_DETAILS) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }
        Hash addressHash = new Hash(addressDetails[ADDRESS_DETAILS_HASH_PLACEMENT]);
        BigDecimal addressAmount = new BigDecimal(addressDetails[ADDRESS_DETAILS_AMOUNT_PLACEMENT]);
        log.trace("The hash {} was loaded from the clusterstamp with amount {}", addressHash, addressAmount);

        balanceService.updateBalanceFromClusterStamp(addressHash, addressAmount);
        byte[] addressHashInBytes = addressHash.getBytes();
        byte[] addressAmountInBytes = addressAmount.stripTrailingZeros().toPlainString().getBytes();
        byte[] balanceInBytes = ByteBuffer.allocate(addressHashInBytes.length + addressAmountInBytes.length).put(addressHashInBytes).put(addressAmountInBytes).array();
        clusterStampData.getSignatureMessage().add(balanceInBytes);
        clusterStampData.incrementMessageByteSize(balanceInBytes.length);
    }

    private void fillSignatureDataFromLine(ClusterStampData clusterStampData, String line, int signatureRelevantLines) {
        if (signatureRelevantLines > 2) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }

        String[] signatureDetails;
        signatureDetails = line.split(",");
        if (signatureDetails.length != NUMBER_OF_SIGNATURE_LINE_DETAILS) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }
        String signaturePrefix = (signatureRelevantLines == 1) ? "r" : "s";
        if (!signatureDetails[0].equalsIgnoreCase(signaturePrefix)) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }

        if (signatureRelevantLines == 1) {
            SignatureData signature = new SignatureData();
            clusterStampData.setSignature(signature);
            clusterStampData.getSignature().setR(signatureDetails[1]);
        } else
            clusterStampData.getSignature().setS(signatureDetails[1]);
    }

    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData) {
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