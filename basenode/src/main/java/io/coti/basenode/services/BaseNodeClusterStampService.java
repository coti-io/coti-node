package io.coti.basenode.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.crypto.GetClusterStampFileNamesCrypto;
import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.http.GetClusterStampFileNames;
import io.coti.basenode.model.ClusterStampNames;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BaseNodeClusterStampService implements IClusterStampService {

    @Value("${logging.file.name}")
    protected String clusterStampFilePrefix;

    protected static final String CLUSTERSTAMP_FILE_SUFFIX = "_clusterstamp.csv";
    private static final int NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES = 1; // Genesis One and Two + heading
    private static final int NUMBER_OF_ADDRESS_LINE_DETAILS = 2;
    private static final int ADDRESS_DETAILS_HASH_PLACEMENT = 0;
    private static final int ADDRESS_DETAILS_AMOUNT_PLACEMENT = 1;
    protected static final String BAD_CSV_FILE_FORMAT = "Bad csv file format";
    private static final String SIGNATURE_LINE_TOKEN = "# Signature";
    private static final int NUMBER_OF_SIGNATURE_LINE_DETAILS = 2;
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
    private ClusterStampNames clusterStampNames;
    @Autowired
    private BaseNodeNetworkService baseNodeNetworkService;
    @Autowired
    private IAwsService awsService;

    @Override
    public void loadClusterStamp() {
        String clusterStampFileLocation = clusterStampFilePrefix + CLUSTERSTAMP_FILE_SUFFIX;
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
            log.info("Clusterstamp is loaded");
        } catch (Exception e) {
            log.error("Errors on clusterstamp loading");
            throw new ClusterStampValidationException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<GetClusterStampFileNames> getRequiredClusterStampNames(GetClusterStampFileNames getClusterStampFileNamesRequest) {
        GetClusterStampFileNames getClusterStampFileNamesResponse = new GetClusterStampFileNames();

        if(!getClusterStampFileNamesCrypto.verifySignature(getClusterStampFileNamesRequest)){
            log.error("Bad signature for {} request", getClusterStampFileNamesCrypto.getClass());
            getClusterStampFileNamesCrypto.signMessage(getClusterStampFileNamesResponse);
            //TODO 8/14/2019 astolia: return some bad response. need to extend with response? create other class for response?
            return ResponseEntity.ok(getClusterStampFileNamesResponse);
        }
        List<String> localeExistingMinors = new ArrayList<>();
        Optional<String> localMajorOptional = clusterStampNames.getMajorAndSetTokens(localeExistingMinors);
        // Assumption - as backup server, this node will have the most updated major.
        if(!localMajorOptional.isPresent()){
            log.error("Major cluster stamp file not found in recovery server.");
            //TODO 8/14/2019 astolia: add some kind of recovery flow to get the cluster stamp from s3?
            //TODO 8/14/2019 astolia: return empty response
        }
        //TODO 8/14/2019 astolia: check what to do if the received has no major
        else if(localMajorOptional.get().equals(getClusterStampFileNamesRequest.getMajor())){
            localeExistingMinors.removeIf(localeExistingMinor -> getClusterStampFileNamesRequest.getTokens().contains(localeExistingMinor));
        }
        else {
            getClusterStampFileNamesResponse.setMajor(localMajorOptional.get());
            if(isClusterStampUpdated(localMajorOptional.get(), getClusterStampFileNamesRequest.getMajor())){
                localeExistingMinors.removeIf(localeExistingMinor -> getClusterStampFileNamesRequest.getTokens().contains(localeExistingMinor));
            }
        }
        getClusterStampFileNamesResponse.setTokens(new HashSet<>(localeExistingMinors));
        getClusterStampFileNamesCrypto.signMessage(getClusterStampFileNamesResponse);
        return ResponseEntity.ok(getClusterStampFileNamesResponse);
    }

    @Override
    public void getClusterStampFromBackupNode(){
        List<String> tokenNames = new ArrayList<>();
        Optional<String> majorOptional = clusterStampNames.getMajorAndSetTokens(tokenNames);
        GetClusterStampFileNames getClusterStampFileNamesRequest = new GetClusterStampFileNames();
        if(majorOptional.isPresent()){
            getClusterStampFileNamesRequest.setMajor(majorOptional.get());
            getClusterStampFileNamesRequest.setTokens(new HashSet<>(tokenNames));
        }
        try{
            RestTemplate restTemplate = new RestTemplate();
            getClusterStampFileNamesCrypto.signMessage(getClusterStampFileNamesRequest);
            ResponseEntity<GetClusterStampFileNames> requiredClusterStampNamesResponse = restTemplate.postForEntity(baseNodeNetworkService.getRecoveryServerAddress() + "/clusterstamps",getClusterStampFileNamesRequest,GetClusterStampFileNames.class);
            if(requiredClusterStampNamesResponse.getStatusCode().is2xxSuccessful()) {

                boolean majorChanged = false;
                GetClusterStampFileNames getClusterStampFileNamesResponse = requiredClusterStampNamesResponse.getBody();
                if (!getClusterStampFileNamesCrypto.verifySignature(getClusterStampFileNamesResponse)) {
                    log.error("Cluster stamp retrieval failed! Bad signature for {} response.", getClusterStampFileNamesCrypto.getClass());
                    return;
                }
                if (getClusterStampFileNamesResponse.getMajor() != null) {
                    awsService.downloadClusterStampFile(getClusterStampFileNamesResponse.getMajor());
                    majorChanged = true;
                }
                getClusterStampFileNamesResponse.getTokens().forEach( tokenClusterStampFileName ->
                {
                    try {
                        awsService.downloadClusterStampFile(tokenClusterStampFileName);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                });

                //TODO 8/15/2019 astolia: handle all downloaded cluster stamp files. also delete old files if needed

            }
        }
        //TODO 8/13/2019 astolia: handle cases like serialization (validation) failure or other server is down, etc...
        catch (Exception e) {
            log.error("Some kind of exception has occurred.");
            log.info(e.getMessage());
        }

    }

    private boolean isClusterStampUpdated(String localMajor, String remoteMajor) {
        String[] localMajorSplit = localMajor.split("_");
        String[] remoteMajorSplit = remoteMajor.split("_");
        if(localMajorSplit.length == remoteMajorSplit.length && remoteMajorSplit.length == 3){
            return false;
        }
        else if(localMajorSplit.length == remoteMajorSplit.length && remoteMajorSplit.length == 4){
            return localMajorSplit[2] == remoteMajorSplit[2];
        }
        return false;
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