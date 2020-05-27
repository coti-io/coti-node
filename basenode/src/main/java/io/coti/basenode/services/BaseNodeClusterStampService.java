package io.coti.basenode.services;

import com.google.gson.Gson;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.GetClusterStampFileNamesCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.data.messages.StateMessageClusterStampExecutePayload;
import io.coti.basenode.data.messages.StateMessageClusterStampInitiatedPayload;
import io.coti.basenode.data.messages.StateMessageLastClusterStampIndexPayload;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.exceptions.FileSystemException;
import io.coti.basenode.http.GetClusterStampFileNamesResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.SerializableResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.GeneralVoteResults;
import io.coti.basenode.model.LastClusterStampVersions;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.CLUSTERSTAMP_MAJOR_NOT_FOUND;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public abstract class BaseNodeClusterStampService implements IClusterStampService {

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
    private static final int CLUSTERSTAMP_CANDIDATE_NAME_ARRAY_NOT_UPDATED_LENGTH = 4;
    private static final int CLUSTERSTAMP_CANDIDATE_NAME_ARRAY_LENGTH = 5;
    private static final int CLUSTERSTAMP_CANDIDATE_VERSION_TIME = 2;
    private static final int CLUSTERSTAMP_CANDIDATE_UPDATE_TIME = 3;
    private static final int CLUSTERSTAMP_CANDIDATE_HASH_NOT_UPDATED_INDEX = 3;
    private static final int CLUSTERSTAMP_CANDIDATE_HASH_UPDATED_INDEX = 4;
    private static final int CLUSTERSTAMP_CANDIDATE_FILE_NAME_PREFIX_INDEX = 0;
    private static final int CLUSTERSTAMP_CANDIDATE_FILE_TYPE_SUFFIX_INDEX = 1;
    private static final int CLUSTERSTAMP_CANDIDATE_PREFIX_AND_SUFFIX_ARRAY_LENGTH = 2;
    private static final int NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES = 1; // Genesis One and Two + heading
    private static final int DETAILS_IN_CLUSTERSTAMP_LINE_WITHOUT_CURRENCY_HASH = 2;
    private static final int DETAILS_IN_CLUSTERSTAMP_LINE_WITH_CURRENCY_HASH = 3;
    private static final int ADDRESS_HASH_INDEX_IN_CLUSTERSTAMP_LINE = 0;
    private static final int AMOUNT_INDEX_IN_CLUSTERSTAMP_LINE = 1;
    private static final int CURRENCY_HASH_INDEX_IN_CLUSTERSTAMP_LINE = 2;
    private static final int NUMBER_OF_SIGNATURE_LINE_DETAILS = 2;
    private static final int LONG_MAX_LENGTH = 19;
    private static final int NUMBER_OF_CURRENCY_GENESIS_ADDRESS_MIN_LINES = 4; // Heading + Genesis + Heading + Native currency
    private static final String BAD_CSV_FILE_FORMAT = "Bad csv file format";
    private static final String SIGNATURE_LINE_TOKEN = "# Signature";
    private static final String CLUSTERSTAMP_FILE_PREFIX = "clusterstamp";
    private static final String CLUSTERSTAMP_FILE_TYPE = "csv";
    private static final String CLUSTERSTAMP_ENDPOINT = "/clusterstamps";
    private static final String CLUSTERSTAMP_DELIMITER = ",";
    protected static final String CURRENCY_GENESIS_ADDRESS_HEADER = "# Currency Genesis Address";
    protected static final String CURRENCIES_DETAILS_HEADER = "# Currencies Details";
    public static final String FOLDER_DELIMITER = "/";
    protected ClusterStampNameData currencyClusterStampName;
    protected ClusterStampNameData balanceClusterStampName;
    @Value("${clusterstamp.folder}")
    protected String clusterStampFolder;
    protected String clusterStampBucketName;
    protected String candidateClusterStampFolder;
    protected String candidateClusterStampBucketName;
    @Value("${application.name}")
    private String applicationName;
    @Value("${get.cluster.stamp.from.recovery.server:true}")
    private boolean getClusterStampFromRecoveryServer;
    private Hash currencyGenesisAddress;
    protected Hash candidateCurrencyClusterStampHash;
    protected Hash candidateBalanceClusterStampHash;
    private SortedMap<Hash, CurrencyData> currencySortedMap;
    private GeneralVoteResult generalVoteResult;

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
    protected Currencies currencies;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected IMintingService mintingService;
    @Autowired
    protected GeneralVoteResults generalVoteResults;

    @Override
    public void init() {
        try {
            fileSystemService.createFolder(clusterStampFolder);
            initLocalClusterStampNames();
            fillClusterStampNamesMap();
            if (getClusterStampFromRecoveryServer) {
                getClusterStampFromRecoveryServer();
            }
            loadAllClusterStamps();
            log.info("{} is up", this.getClass().getSimpleName());
        } catch (ClusterStampException e) {
            throw new ClusterStampException("Error at clusterstamp init.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException("Error at clusterstamp init.", e);
        }
    }

    private void initLocalClusterStampNames() {
        currencyClusterStampName = null;
        balanceClusterStampName = null;
    }

    protected void fillClusterStampNamesMap() {
        List<String> clusterStampFileNames = fileSystemService.listFolderFileNames(clusterStampFolder);
        for (String clusterStampFileName : clusterStampFileNames) {
            ClusterStampNameData clusterStampNameData = validateNameAndGetClusterStampNameData(clusterStampFileName);
            if (clusterStampNameData.isCurrency() && currencyClusterStampName != null) {
                throw new ClusterStampException(String.format("Error, Multiple local currencies clusterstamps found: [%s, %s] .Please remove excess clusterstamps and restart.", currencyClusterStampName, getClusterStampFileName(clusterStampNameData)));
            }
            if (clusterStampNameData.isBalance() && balanceClusterStampName != null) {
                throw new ClusterStampException(String.format("Error, Multiple local balance clusterstamps found: [%s, %s] .Please remove excess clusterstamps and restart.", balanceClusterStampName, getClusterStampFileName(clusterStampNameData)));
            }
            addClusterStampName(clusterStampNameData);
        }
    }

    protected ClusterStampNameData validateNameAndGetClusterStampNameData(String clusterStampFileName) {
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

    protected ClusterStampNameData validateNameAndGetCandidateClusterStampNameData(String clusterStampFileName) {
        String[] clusterStampNamePrefixSuffixDelimitedPart = clusterStampFileName.split("\\.");
        if (clusterStampNamePrefixSuffixDelimitedPart.length != CLUSTERSTAMP_CANDIDATE_PREFIX_AND_SUFFIX_ARRAY_LENGTH) {
            throw new ClusterStampException(String.format("Bad cluster stamp file name structure: %s.", clusterStampFileName));
        }

        String[] delimitedFileName = clusterStampNamePrefixSuffixDelimitedPart[CLUSTERSTAMP_CANDIDATE_FILE_NAME_PREFIX_INDEX].split("_");
        if (delimitedFileName.length != CLUSTERSTAMP_CANDIDATE_NAME_ARRAY_NOT_UPDATED_LENGTH && delimitedFileName.length != CLUSTERSTAMP_CANDIDATE_NAME_ARRAY_LENGTH) {
            throw new ClusterStampValidationException(String.format("Bad candidate cluster stamp file name: %s. Please correct candidate clusterstamp file name", clusterStampFileName));
        }
        String clusterStampFileType = clusterStampNamePrefixSuffixDelimitedPart[CLUSTERSTAMP_CANDIDATE_FILE_TYPE_SUFFIX_INDEX];
        String clusterStampConstantPrefix = delimitedFileName[CLUSTERSTAMP_CONST_PREFIX_INDEX];
        String clusterStampTypeMark = delimitedFileName[CLUSTERSTAMP_TYPE_MARK_INDEX];

        String clusterStampVersionTime = delimitedFileName[CLUSTERSTAMP_CANDIDATE_VERSION_TIME];
        String clusterStampUpdateTime;
        String clusterStampHash;
        if (delimitedFileName.length == CLUSTERSTAMP_CANDIDATE_NAME_ARRAY_NOT_UPDATED_LENGTH) {
            clusterStampUpdateTime = clusterStampVersionTime;
            clusterStampHash = delimitedFileName[CLUSTERSTAMP_CANDIDATE_HASH_NOT_UPDATED_INDEX];
        } else {
            clusterStampUpdateTime = delimitedFileName[CLUSTERSTAMP_CANDIDATE_UPDATE_TIME];
            clusterStampHash = delimitedFileName[CLUSTERSTAMP_CANDIDATE_HASH_UPDATED_INDEX];
        }

        if (!validateCandidateClusterStampFileName(clusterStampConstantPrefix, clusterStampTypeMark, clusterStampVersionTime, clusterStampUpdateTime, clusterStampFileType, clusterStampHash)) {
            throw new ClusterStampValidationException(String.format("Bad candidate cluster stamp file name: %s. Please correct clusterstamp name and restart.", clusterStampFileName));
        }
        return new ClusterStampNameData(ClusterStampType.getTypeByMark(clusterStampTypeMark).get(), clusterStampVersionTime, clusterStampUpdateTime);
    }

    private boolean validateCandidateClusterStampFileName(String clusterStampConstantPrefix, String clusterStampTypeMark, String clusterStampVersionTime, String clusterStampUpdateTime, String clusterStampFileType, String clusterStampHash) {
        try {
            DatatypeConverter.parseHexBinary(clusterStampHash);
        } catch (Exception e) {
            log.error("Illegal hash string: {}", clusterStampHash);
            return false;
        }
        return validateClusterStampFileName(clusterStampConstantPrefix, clusterStampTypeMark, clusterStampVersionTime, clusterStampUpdateTime, clusterStampFileType);
    }

    private void loadAllClusterStamps() {
        log.info("Loading clusterstamp files");
        Map<Hash, CurrencyData> currencyMap = new HashMap<>();
        loadCurrencyClusterStamp(currencyClusterStampName, currencyMap, shouldUpdateClusterStampDBVersion(), false);
        Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap = new HashMap<>();
        currencyMap.forEach((currencyHash, currencyData) -> {
            if (currencyData.isConfirmed()) {
                clusterStampCurrencyMap.put(currencyHash, new ClusterStampCurrencyData(currencyData));
            }
        });
        loadBalanceClusterStamp(balanceClusterStampName, clusterStampCurrencyMap, false);
    }

    protected void addClusterStampName(ClusterStampNameData clusterStampNameData) {
        if (clusterStampNameData.isCurrency()) {
            currencyClusterStampName = clusterStampNameData;
        } else if (clusterStampNameData.isBalance()) {
            balanceClusterStampName = clusterStampNameData;
        }
    }

    private void removeClusterStampName(ClusterStampNameData clusterStampNameData) {
        if (clusterStampNameData.isCurrency()) {
            currencyClusterStampName = null;
        } else if (clusterStampNameData.isBalance()) {
            balanceClusterStampName = null;
        }
    }

    protected String getClusterStampFileName(ClusterStampNameData clusterStampNameData) {
        StringBuilder sb = getClusterStampFileNameBody(clusterStampNameData);
        return sb.append(".").append(CLUSTERSTAMP_FILE_TYPE).toString();
    }

    private StringBuilder getClusterStampFileNameBody(ClusterStampNameData clusterStampNameData) {
        Long versionTimeMillis = clusterStampNameData.getVersionTimeMillis();
        Long creationTimeMillis = clusterStampNameData.getCreationTimeMillis();
        StringBuilder sb = new StringBuilder(CLUSTERSTAMP_FILE_PREFIX);
        sb.append("_").append(clusterStampNameData.getType().getMark()).append("_").append(versionTimeMillis.toString());
        if (!versionTimeMillis.equals(creationTimeMillis)) {
            sb.append("_").append(creationTimeMillis.toString());
        }
        return sb;
    }

    private String getCandidateClusterStampFileName(ClusterStampNameData clusterStampNameData) {
        StringBuilder sb = getClusterStampFileNameBody(clusterStampNameData);
        sb.append("_").append(networkService.getNetworkNodeData().getNodeHash().toHexString());
        return sb.toString();
    }

    protected void loadCurrencyClusterStamp(ClusterStampNameData currencyClusterStampNameData, Map<Hash, CurrencyData> currencyMap,
                                            boolean updateCurrencies, boolean hashCalculation) {
        String clusterStampFileName = getClusterStampFileName(currencyClusterStampNameData);
        log.info("Starting to load currency clusterstamp file {}", clusterStampFileName);
        String clusterStampFileLocation = clusterStampFolder + clusterStampFileName;
        File clusterstampFile = new File(clusterStampFileLocation);
        ClusterStampData clusterStampData = new ClusterStampData();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(clusterstampFile))) {
            String line;
            boolean reachedCurrenciesSection = false;
            AtomicBoolean reachedSignatureSection = new AtomicBoolean(false);
            boolean finishedCurrencies = false;
            AtomicInteger relevantLineNumber = new AtomicInteger(0);
            AtomicInteger signatureRelevantLines = new AtomicInteger(0);

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                relevantLineNumber.incrementAndGet();
                if (line.isEmpty()) {
                    if (relevantLineNumber.get() < NUMBER_OF_CURRENCY_GENESIS_ADDRESS_MIN_LINES) {
                        throw new ClusterStampValidationException(String.format("Currency clusterstamp file %s has less than necessary number of currency lines", clusterStampFileName));
                    } else {
                        if (!finishedCurrencies)
                            finishedCurrencies = true;
                        else
                            throw new ClusterStampValidationException(String.format("Unnecessary empty line at currency clusterstamp file %s.", clusterStampFileName));
                    }
                } else {
                    if (!reachedCurrenciesSection) {
                        if (!line.contentEquals(CURRENCY_GENESIS_ADDRESS_HEADER)) {
                            throw new ClusterStampValidationException(String.format("Currency clusterstamp file %s expected currency genesis address header", clusterStampFileName));
                        } else {
                            line = bufferedReader.readLine();
                            relevantLineNumber.incrementAndGet();
                            line = line.trim();
                            if (line.isEmpty()) {
                                throw new ClusterStampValidationException(String.format("Currency clusterstamp file %s expected currency genesis address", clusterStampFileName));
                            } else {
                                currencyGenesisAddress = new Hash(line);
                                byte[] genesisAddressInBytes = currencyGenesisAddress.getBytes();
                                clusterStampData.getSignatureMessage().add(genesisAddressInBytes);
                                clusterStampData.incrementMessageByteSize(genesisAddressInBytes.length);
                                line = bufferedReader.readLine();
                                relevantLineNumber.incrementAndGet();
                                line = line.trim();
                                if (line.isEmpty() || !line.contentEquals(CURRENCIES_DETAILS_HEADER)) {
                                    throw new ClusterStampValidationException(String.format("Currency clusterstamp file %s expected currencies header", clusterStampFileName));
                                } else {
                                    reachedCurrenciesSection = true;
                                }
                            }
                        }
                    } else {
                        if (!finishedCurrencies) {
                            byte[] currencyDataInBytes = Base64.getDecoder().decode(line);
                            CurrencyData currencyData = (CurrencyData) SerializationUtils.deserialize(currencyDataInBytes);
                            if (currencyData == null) {
                                throw new ClusterStampValidationException(String.format("Currency clusterstamp file %s contains invalid currency line", clusterStampFileName));
                            }
                            currencyMap.put(currencyData.getHash(), currencyData);

                            log.trace("The currency hash {} was loaded from the currency clusterstamp", currencyData.getHash());
                            clusterStampData.getSignatureMessage().add(currencyDataInBytes);
                            clusterStampData.incrementMessageByteSize(currencyDataInBytes.length);
                        } else {
                            if (!reachedSignatureSection.get()) {
                                if (!line.contentEquals(SIGNATURE_LINE_TOKEN)) {
                                    throw new ClusterStampValidationException(String.format("Invalid signature line notification at currencies clusterstamp file %s", clusterStampFileName));
                                } else {
                                    reachedSignatureSection.set(true);
                                }
                            } else {
                                signatureRelevantLines.incrementAndGet();
                                fillSignatureDataFromLine(clusterStampData, line, signatureRelevantLines);
                            }
                        }
                    }
                }
            }
            if (signatureRelevantLines.get() == 0) {
                handleClusterStampWithoutSignature(clusterStampData, clusterStampFileLocation);
            } else if (signatureRelevantLines.get() == 1) {
                throw new ClusterStampValidationException(String.format("Signature lines can not be a single line at currencies clusterstamp file %s", clusterStampFileName));
            } else {
                handleClusterStampWithSignature(clusterStampData);
            }
            if (!hashCalculation) {
                if (updateCurrencies) {
                    currencyService.updateCurrenciesFromClusterStamp(currencyMap);
                }
            } else {
                setCandidateCurrencyClusterStampHash(calculateClusterStampDataMessageHash(clusterStampData));
            }
            log.info("Finished to load currency clusterstamp file {}", clusterStampFileName);
        } catch (ClusterStampException e) {
            throw new ClusterStampException(String.format("Errors on currency clusterstamp file %s loading.%n", clusterStampFileName) + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Errors on currency clusterstamp file %s loading.", clusterStampFileName), e);
        }
    }

    protected void loadBalanceClusterStamp(ClusterStampNameData clusterStampNameData, Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap,
                                           boolean hashCalculation) {
        String clusterStampFileName = getClusterStampFileName(clusterStampNameData);
        log.info("Starting to load balance clusterstamp file {}", clusterStampFileName);
        String clusterStampFileLocation = clusterStampFolder + clusterStampFileName;
        File clusterStampFile = new File(clusterStampFileLocation);
        ClusterStampData clusterStampData = new ClusterStampData();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(clusterStampFile))) {
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
            if (signatureRelevantLines.get() == 0) {
                handleClusterStampWithoutSignature(clusterStampData, clusterStampFileLocation);
            } else if (signatureRelevantLines.get() == 1) {
                throw new ClusterStampValidationException(String.format("Signature lines can not be a single line at clusterstamp file %s", clusterStampFileName));
            } else {
                handleClusterStampWithSignature(clusterStampData);
            }
            if (!hashCalculation) {
                mintingService.updateMintingAvailableMapFromClusterStamp(clusterStampCurrencyMap);
            } else {
                setCandidateBalanceClusterStampHash(calculateClusterStampDataMessageHash(clusterStampData));
            }

            log.info("Finished to load balance clusterstamp file {}", clusterStampFileName);
        } catch (ClusterStampException e) {
            throw new ClusterStampException(String.format("Errors on balance clusterstamp file %s loading.%n", clusterStampFileName) + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Errors on balance clusterstamp file %s loading.", clusterStampFileName), e);
        }
    }

    protected void handleMissingRecoveryServer() {
        throw new ClusterStampException("Recovery server undefined.");
    }

    @Override
    public void getClusterStampFromRecoveryServer() {
        String recoveryServerAddress = networkService.getRecoveryServerAddress();
        if (recoveryServerAddress == null) {
            handleMissingRecoveryServer();
            return;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            GetClusterStampFileNamesResponse getClusterStampFileNamesResponse = restTemplate.getForObject(recoveryServerAddress + CLUSTERSTAMP_ENDPOINT, GetClusterStampFileNamesResponse.class);
            if (getClusterStampFileNamesResponse == null) {
                throw new ClusterStampException(String.format("Cluster stamp retrieval failed. Null response from recovery server %s.", recoveryServerAddress));
            }
            if (!getClusterStampFileNamesCrypto.verifySignature(getClusterStampFileNamesResponse)) {
                throw new ClusterStampException(String.format("Cluster stamp retrieval failed. Bad signature for response from recovery server %s.", recoveryServerAddress));
            }
            clusterStampBucketName = getClusterStampFileNamesResponse.getClusterStampBucketName();
            handleRequiredClusterStampFiles(getClusterStampFileNamesResponse);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new ClusterStampException(String.format("Clusterstamp recovery failed. Recovery server response: %s.", new Gson().fromJson(e.getResponseBodyAsString(), Response.class).getMessage()), e);
        } catch (ClusterStampException e) {
            throw new ClusterStampException("Clusterstamp recovery failed.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException("Clusterstamp recovery failed.", e);
        }

    }

    private void handleRequiredClusterStampFiles(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse) {
        if (!validateResponseVersionValidity(getClusterStampFileNamesResponse)) {
            throw new ClusterStampValidationException("Recovery clusterstamp version is not valid");
        }
        ClusterStampNameData recoveryCurrencyClusterStampName = getClusterStampFileNamesResponse.getCurrencyClusterStampName();
        ClusterStampNameData recoveryBalanceClusterStampName = getClusterStampFileNamesResponse.getBalanceClusterStampName();
        if (currencyClusterStampName == null || recoveryCurrencyClusterStampName.getVersionTimeMillis() > currencyClusterStampName.getVersionTimeMillis()) {
            handleMissingClusterStamp(currencyClusterStampName, recoveryCurrencyClusterStampName);
        }
        if (balanceClusterStampName == null || recoveryBalanceClusterStampName.getVersionTimeMillis() > balanceClusterStampName.getVersionTimeMillis()) {
            handleMissingClusterStamp(balanceClusterStampName, recoveryBalanceClusterStampName);
        }
    }

    private boolean validateResponseVersionValidity(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse) {
        LastClusterStampVersionData lastVersionData = lastClusterStampVersions.get();
        ClusterStampNameData recoveryCurrencyClusterStampName = getClusterStampFileNamesResponse.getCurrencyClusterStampName();
        ClusterStampNameData recoveryBalanceClusterStampName = getClusterStampFileNamesResponse.getBalanceClusterStampName();
        return recoveryCurrencyClusterStampName != null &&
                recoveryBalanceClusterStampName != null && recoveryCurrencyClusterStampName.getVersionTimeMillis().equals(recoveryBalanceClusterStampName.getVersionTimeMillis()) &&
                (lastVersionData == null || lastVersionData.getVersionTimeMillis() == null ||
                        validateVersion(recoveryCurrencyClusterStampName.getVersionTimeMillis(), lastVersionData.getVersionTimeMillis()));
    }

    private boolean validateVersion(Long clusterStampFileVersion, Long clusterStampDBVersion) {
        return clusterStampFileVersion >= clusterStampDBVersion;
    }

    private void handleMissingClusterStamp(ClusterStampNameData localClusterStampName, ClusterStampNameData recoveryClusterStampName) {
        if (localClusterStampName != null) {
            removeClusterStampNameAndFile(localClusterStampName);
        }
        downloadAndAddSingleClusterStamp(recoveryClusterStampName);
    }

    @Override
    public boolean shouldUpdateClusterStampDBVersion() {
        LastClusterStampVersionData lastVersionData = lastClusterStampVersions.get();
        return lastVersionData == null || lastVersionData.getVersionTimeMillis() == null || currencyClusterStampName.getVersionTimeMillis() > lastVersionData.getVersionTimeMillis();
    }

    @Override
    public boolean isClusterStampDBVersionExist() {
        return lastClusterStampVersions.get() != null;
    }

    @Override
    public void setClusterStampDBVersion() {
        lastClusterStampVersions.put(new LastClusterStampVersionData(currencyClusterStampName.getVersionTimeMillis()));
        log.info("Clusterstamp version time is set to {}", Instant.ofEpochMilli(currencyClusterStampName.getVersionTimeMillis()));
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

    @Override
    public ResponseEntity<IResponse> getRequiredClusterStampNames() {
        GetClusterStampFileNamesResponse getClusterStampFileNamesResponse = new GetClusterStampFileNamesResponse();
        if (balanceClusterStampName == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SerializableResponse(CLUSTERSTAMP_MAJOR_NOT_FOUND, STATUS_ERROR));
        }
        getClusterStampFileNamesResponse.setCurrencyClusterStampName(currencyClusterStampName);
        getClusterStampFileNamesResponse.setBalanceClusterStampName(balanceClusterStampName);
        getClusterStampFileNamesResponse.setClusterStampBucketName(clusterStampBucketName);
        getClusterStampFileNamesCrypto.signMessage(getClusterStampFileNamesResponse);
        return ResponseEntity.ok(getClusterStampFileNamesResponse);
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
            balanceService.updateBalanceAndPreBalanceFromClusterStamp(addressHash, currencyHash, currencyAmountInAddress);
            log.trace("The address hash {} for currency hash {} was loaded from the clusterstamp {} with amount {}", addressHash, currencyHash, clusterStampFileName, currencyAmountInAddress);

            byte[] addressHashInBytes = addressHash.getBytes();
            byte[] addressCurrencyAmountInBytes = currencyAmountInAddress.stripTrailingZeros().toPlainString().getBytes();
            byte[] currencyHashInBytes = numOfDetailsInLine == DETAILS_IN_CLUSTERSTAMP_LINE_WITH_CURRENCY_HASH ? currencyHash.getBytes() : new byte[0];
            byte[] balanceInBytes = ByteBuffer.allocate(addressHashInBytes.length + addressCurrencyAmountInBytes.length + currencyHashInBytes.length)
                    .put(addressHashInBytes).put(addressCurrencyAmountInBytes).put(currencyHashInBytes).array();
            clusterStampData.getSignatureMessage().add(balanceInBytes);
            clusterStampData.incrementMessageByteSize(balanceInBytes.length);
        } catch (ClusterStampException e) {
            throw new ClusterStampException(String.format("Error at filling balance from line of clusterstamp %s.%n", clusterStampFileName) + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Error at filling balance from line of clusterstamp %s.", clusterStampFileName), e);
        }
    }

    private void validateClusterStampLineDetails(BigDecimal currencyAmountInAddress, Hash
            currencyHash, Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap, String clusterStampFileName) {
        ClusterStampCurrencyData clusterStampCurrencyData = clusterStampCurrencyMap.get(currencyHash);
        if (clusterStampCurrencyData == null) {
            throw new ClusterStampValidationException(String.format("Currency %s in clusterstamp file %s not found at DB", currencyHash, clusterStampFileName));
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

    private void fillSignatureDataFromLine(ClusterStampData clusterStampData, String line, AtomicInteger signatureRelevantLines) {
        if (signatureRelevantLines.get() > 2) {
            return;
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

    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData, String clusterStampFileLocation) {
        throw new ClusterStampValidationException(String.format("Clusterstamp file %s has no signature.", clusterStampFileLocation));
    }

    private void handleClusterStampWithSignature(ClusterStampData clusterStampData) {
        setClusterStampSignerHash(clusterStampData);
        if (!clusterStampCrypto.verifySignature(clusterStampData)) {
            throw new ClusterStampValidationException("Invalid signature.");
        }
    }

    protected void addVotesToClusterStamp(ClusterStampData clusterStampData, String clusterStampFileLocation) {
        throw new ClusterStampValidationException(String.format("Clusterstamp file %s illegal attempt to add votes.", clusterStampFileLocation));
    }

    protected void setClusterStampSignerHash(ClusterStampData clusterStampData) {
        clusterStampData.setSignerHash(networkService.getSingleNodeData(NodeType.ZeroSpendServer).getNodeHash());
    }

    @Override
    public void clusterStampInitiate(StateMessage stateMessage, StateMessageClusterStampInitiatedPayload stateMessageClusterstampInitiatedPayload) {
        // implemented in subclasses
    }

    @Override
    public boolean checkLastConfirmedIndex(StateMessageLastClusterStampIndexPayload stateMessageLastClusterStampIndexPayload) {
        // implemented in subclasses
        return false;
    }

    @Override
    public void clusterStampExecute(StateMessage stateMessage, StateMessageClusterStampExecutePayload stateMessageClusterStampExecutePayload) {
        // implemented in subclasses
    }

    private void createClusterStampFiles(boolean uploadFile) {
        Long versionTimeMillis = Instant.now().toEpochMilli();
        String versionTimeMillisString = String.valueOf(versionTimeMillis);
        boolean deleteLocalCopy = true;

        createCandidateCurrencyClusterStampFile(uploadFile, versionTimeMillisString, deleteLocalCopy);
        createCandidateBalanceClusterStampFile(uploadFile, versionTimeMillisString, deleteLocalCopy);
    }

    private void createCandidateCurrencyClusterStampFile(boolean uploadFile, String versionTimeMillisString, boolean deleteLocalCopy) {
        ClusterStampNameData newCurrencyClusterStampNameData = new ClusterStampNameData(ClusterStampType.CURRENCY, versionTimeMillisString, versionTimeMillisString);
        String candidateCurrencyClusterStampFileName = getCandidateClusterStampFileName(newCurrencyClusterStampNameData);
        String currencyClusterStampFilename = clusterStampFolder + FOLDER_DELIMITER + candidateCurrencyClusterStampFileName;
        ClusterStampData currencyClusterStampData = createCurrencyClusterStamp(currencyClusterStampFilename);
        addVotesToClusterStamp(currencyClusterStampData, candidateCurrencyClusterStampFileName);

        if (uploadFile) {
            uploadCandidateClusterStamp(candidateCurrencyClusterStampFileName);
        }
        if (deleteLocalCopy) {
            fileSystemService.deleteFile(currencyClusterStampFilename);
        }
    }

    private void createCandidateBalanceClusterStampFile(boolean uploadFile, String versionTimeMillisString, boolean deleteLocalCopy) {
        ClusterStampNameData newBalanceClusterStampNameData = new ClusterStampNameData(ClusterStampType.BALANCE, versionTimeMillisString, versionTimeMillisString);
        String candidateBalanceClusterStampFileName = getCandidateClusterStampFileName(newBalanceClusterStampNameData);
        String balanceClusterStampFilename = clusterStampFolder + FOLDER_DELIMITER + candidateBalanceClusterStampFileName;
        ClusterStampData balanceClusterStampData = createBalanceClusterStamp(balanceClusterStampFilename);

        addVotesToClusterStamp(balanceClusterStampData, candidateBalanceClusterStampFileName);

        if (uploadFile) {
            uploadCandidateClusterStamp(candidateBalanceClusterStampFileName);
        }
        if (deleteLocalCopy) {
            fileSystemService.deleteFile(balanceClusterStampFilename);
        }
    }

    private ClusterStampData createCurrencyClusterStamp(String currencyClusterStampFilename) {
        sortCurrencies();
        ClusterStampData clusterStampData = new ClusterStampData();
        CurrencyData nativeCurrency = currencyService.getNativeCurrency();
        Hash nativeCurrencyAddress;
        if (nativeCurrency != null) {
            nativeCurrencyAddress = nativeCurrency.getHash();
            byte[] genesisAddressInBytes = nativeCurrencyAddress.getBytes();
            clusterStampData.getSignatureMessage().add(genesisAddressInBytes);
            clusterStampData.incrementMessageByteSize(genesisAddressInBytes.length);
        } else {
            throw new ClusterStampException("Unable to start cluster stamp. Genesis address not found.");
        }


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currencyClusterStampFilename))) {
            writeNativeCurrencyDetails(nativeCurrency, writer, String.valueOf(nativeCurrencyAddress));
            generateCurrencyLines(clusterStampData, writer);
            writeSignature(clusterStampData, writer);
        } catch (IOException e) {
            throw new FileSystemException(String.format("Create and write file error. %s: %s", e.getClass().getName(), e.getMessage()));
        }
        setCandidateCurrencyClusterStampHash(calculateClusterStampDataMessageHash(clusterStampData));
        return clusterStampData;
    }

    private void generateCurrencyLines(ClusterStampData clusterStampData, BufferedWriter writer) throws IOException {
        for (Map.Entry<Hash, CurrencyData> additionalCurrencyData : currencySortedMap.entrySet()) {
            if (!additionalCurrencyData.getValue().getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN)) {
                String serializedCurrencyData = Base64.getEncoder().encodeToString(SerializationUtils.serialize(additionalCurrencyData));
                writer.write(serializedCurrencyData);
                writer.newLine();
                byte[] currencyDataInBytes = Base64.getDecoder().decode(serializedCurrencyData);
                clusterStampData.getSignatureMessage().add(currencyDataInBytes);
                clusterStampData.incrementMessageByteSize(currencyDataInBytes.length);
            }
        }
    }

    private void sortCurrencies() {
        currencySortedMap = new TreeMap();
        if (!currencies.isEmpty()) {
            currencies.forEach(currencyData -> currencySortedMap.put(currencyData.getHash(), currencyData));
        } else {
            throw new ClusterStampException("Unable to start cluster stamp. Currencies not found.");
        }
    }

    private void writeSignature(ClusterStampData clusterStampData, BufferedWriter writer) throws IOException {
        clusterStampCrypto.signMessage(clusterStampData);
        writer.newLine();
        writer.append(SIGNATURE_LINE_TOKEN);
        writer.newLine();
        writer.append("r," + clusterStampData.getSignature().getR());
        writer.newLine();
        writer.append("s," + clusterStampData.getSignature().getS());
    }

    private Hash calculateClusterStampDataMessageHash(ClusterStampData clusterStampData) {
        byte[] streamArray = null;
        try {
            streamArray = IOUtils.toByteArray((InputStream) clusterStampData.getSignatureMessage());
        } catch (IOException e) {
            throw new ClusterStampException("Unable to calculate cluster stamp data message hash.");
        }
        return streamArray != null ? new Hash(streamArray) : null;
    }

    protected void writeNativeCurrencyDetails(CurrencyData nativeCurrency, BufferedWriter writer, String currencyAddress) throws IOException {
        writer.write(CURRENCY_GENESIS_ADDRESS_HEADER);
        writer.newLine();
        writer.write(currencyAddress);
        writer.newLine();
        writer.write(CURRENCIES_DETAILS_HEADER);
        writer.newLine();
        writer.write(Base64.getEncoder().encodeToString(SerializationUtils.serialize(nativeCurrency)));
        writer.newLine();
    }

    private void uploadCandidateClusterStamp(String candidateClusterStampFileName) {
        awsService.uploadFileToS3(candidateClusterStampBucketName, candidateClusterStampFolder + candidateClusterStampFileName);
    }

    private ClusterStampData createBalanceClusterStamp(String balanceClusterStampFilename) {
        ClusterStampData clusterStampData = new ClusterStampData();
        Hash nativeCurrencyHash = currencyService.getNativeCurrencyHash();
        TreeMap<Hash, BigDecimal> sortedBalance = balanceService.getSortedBalance(nativeCurrencyHash);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(balanceClusterStampFilename))) {
            generateCurrencyBalanceLines(clusterStampData, nativeCurrencyHash, sortedBalance, writer);

            currencySortedMap.forEach((currencyHash, currencyData) ->
                    currencySortedMap.keySet().stream().map(hash -> currencySortedMap.get(hash))
                            .filter(additionalCurrencyData -> !additionalCurrencyData.getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN))
                            .forEach(additionalCurrencyData -> generateCurrencyBalanceLines(clusterStampData, additionalCurrencyData.getHash(), sortedBalance, writer))
            );
            writeSignature(clusterStampData, writer);
        } catch (IOException e) {
            throw new FileSystemException(String.format("Create and write file error. %s: %s", e.getClass().getName(), e.getMessage()));
        }
        setCandidateBalanceClusterStampHash(calculateClusterStampDataMessageHash(clusterStampData));
        return clusterStampData;
    }

    private void generateCurrencyBalanceLines(ClusterStampData clusterStampData, Hash currencyHash, TreeMap<Hash, BigDecimal> sortedBalance, BufferedWriter writer) {
        try {
            for (Map.Entry<Hash, BigDecimal> entry : sortedBalance.entrySet()) {
                Hash addressHash = entry.getKey();
                BigDecimal currencyAmountInAddress = entry.getValue();
                StringBuilder sb = new StringBuilder();
                String line = sb.append(addressHash).append(CLUSTERSTAMP_DELIMITER).append(currencyAmountInAddress.toString()).append(CLUSTERSTAMP_DELIMITER).append(currencyHash).toString();
                writer.write(line);
                writer.newLine();
                byte[] addressHashInBytes = addressHash.getBytes();
                byte[] addressCurrencyAmountInBytes = currencyAmountInAddress.stripTrailingZeros().toPlainString().getBytes();
                byte[] currencyHashInBytes = currencyHash.getBytes();
                byte[] balanceInBytes = ByteBuffer.allocate(addressHashInBytes.length + addressCurrencyAmountInBytes.length + currencyHashInBytes.length)
                        .put(addressHashInBytes).put(addressCurrencyAmountInBytes).put(currencyHashInBytes).array();
                clusterStampData.getSignatureMessage().add(balanceInBytes);
                clusterStampData.incrementMessageByteSize(balanceInBytes.length);
            }
        } catch (IOException e) {
            throw new ClusterStampException("Unable to create currency balance lines.");
        }
    }

    public Hash getCandidateCurrencyClusterStampHash() {
        return candidateCurrencyClusterStampHash;
    }

    private void setCandidateCurrencyClusterStampHash(Hash candidateCurrencyClusterStampHash) {
        this.candidateCurrencyClusterStampHash = candidateCurrencyClusterStampHash;
    }

    public Hash getCandidateBalanceClusterStampHash() {
        return candidateBalanceClusterStampHash;
    }

    private void setCandidateBalanceClusterStampHash(Hash candidateBalanceClusterStampHash) {
        this.candidateBalanceClusterStampHash = candidateBalanceClusterStampHash;
    }

    protected GeneralVoteResult getGeneralVoteResult() {
        return generalVoteResult;
    }

    protected void setGeneralVoteResult(GeneralVoteResult generalVoteResult) {
        this.generalVoteResult = generalVoteResult;
    }
}