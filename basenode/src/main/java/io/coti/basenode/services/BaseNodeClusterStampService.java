package io.coti.basenode.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;


/**
 * An abstract class that provides basic Cluster Stamp functionality for all nodes that take part in cluster stamp flow.
 */
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
    protected INetworkService networkService;

    @Override
    public void loadClusterStamp() throws Exception {
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
                        throw new Exception(BAD_CSV_FILE_FORMAT);
                    } else {
                        if (!finishedBalances)
                            finishedBalances = true;
                        else
                            throw new Exception(BAD_CSV_FILE_FORMAT);
                    }
                } else {
                    if (!finishedBalances) {
                        fillBalanceFromLine(clusterStampData, line);
                    } else {
                        if (!reachedSignatureSection) {
                            if (!line.contentEquals(SIGNATURE_LINE_TOKEN))
                                throw new Exception(BAD_CSV_FILE_FORMAT);
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
                throw new Exception(BAD_CSV_FILE_FORMAT);
            } else {
                handleClusterStampWithSignature(clusterStampData);
            }
            balanceService.updatePreBalanceFromClusterStamp();
            log.info("Clusterstamp is loaded");
        } catch (Exception e) {
            log.error("Errors on clusterstamp loading");
            throw e;
        }
    }

    private void fillBalanceFromLine(ClusterStampData clusterStampData, String line) throws Exception {
        String[] addressDetails;
        addressDetails = line.split(",");
        if (addressDetails.length != NUMBER_OF_ADDRESS_LINE_DETAILS) {
            throw new Exception(BAD_CSV_FILE_FORMAT);
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

    private void fillSignatureDataFromLine(ClusterStampData clusterStampData, String line, int signatureRelevantLines) throws Exception {
        if (signatureRelevantLines > 2) {
            throw new Exception(BAD_CSV_FILE_FORMAT);
        }

        String[] signatureDetails;
        signatureDetails = line.split(",");
        if (signatureDetails.length != NUMBER_OF_SIGNATURE_LINE_DETAILS) {
            throw new Exception(BAD_CSV_FILE_FORMAT);
        }
        String signaturePrefix = (signatureRelevantLines == 1) ? "r" : "s";
        if (!signatureDetails[0].equalsIgnoreCase(signaturePrefix)) {
            throw new Exception(BAD_CSV_FILE_FORMAT);
        }

        if (signatureRelevantLines == 1) {
            SignatureData signature = new SignatureData();
            clusterStampData.setSignature(signature);
            clusterStampData.getSignature().setR(signatureDetails[1]);
        } else
            clusterStampData.getSignature().setS(signatureDetails[1]);
    }

    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData) throws Exception {
        throw new Exception(BAD_CSV_FILE_FORMAT);
    }

    private void handleClusterStampWithSignature(ClusterStampData clusterStampData) throws Exception {
        setClusterStampSignerHash(clusterStampData);
        if (!clusterStampCrypto.verifySignature(clusterStampData)) {
            log.error("Clusterstamp invalid signature");
            throw new Exception(BAD_CSV_FILE_FORMAT);
        }
    }

    protected void setClusterStampSignerHash(ClusterStampData clusterStampData) {
        clusterStampData.setSignerHash(networkService.getSingleNodeData(NodeType.ZeroSpendServer).getNodeHash());
    }

}