package io.coti.basenode.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

@Slf4j
@Service
public class BaseNodeClusterStampService implements IClusterStampService {

    @Value("${logging.file.name}")
    protected String clusterStampFilePrefix;
    protected static final String CLUSTERSTAMP_FILE_SUFFIX = "_clusterstamp.csv";
    private static final int NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES = 1; // Genesis One and Two + heading
    private static final int NUMBER_OF_ADDRESS_LINE_DETAILS = 3;
    private static final int NUMBER_OF_ADDRESS_LINE_DETAILS_WITHOUT_CURRENCY_HASH = 2;
    private static final int ADDRESS_DETAILS_HASH_PLACEMENT = 0;
    private static final int ADDRESS_DETAILS_AMOUNT_PLACEMENT = 2;
    private static final int ADDRESS_DETAILS_AMOUNT_PLACEMENT_WITHOUT_CURRENCY_HASH = 1;
    private static final int CURRENCY_DATA_HASH_PLACEMENT = 1;
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
    @Autowired
    protected BaseNodeCurrencyService baseNodeCurrencyService;
    @Autowired
    protected ApplicationContext applicationContext;

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

    private void fillBalanceFromLine(ClusterStampData clusterStampData, String line) {
        String[] addressDetails;
        addressDetails = line.split(",");
        int numOfDetailsInLine = addressDetails.length;
        if (numOfDetailsInLine != NUMBER_OF_ADDRESS_LINE_DETAILS && numOfDetailsInLine != NUMBER_OF_ADDRESS_LINE_DETAILS_WITHOUT_CURRENCY_HASH) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }
        Hash addressHash = new Hash(addressDetails[ADDRESS_DETAILS_HASH_PLACEMENT]);
        BigDecimal addressAmount = numOfDetailsInLine == NUMBER_OF_ADDRESS_LINE_DETAILS ? new BigDecimal(addressDetails[ADDRESS_DETAILS_AMOUNT_PLACEMENT]) :
                new BigDecimal(addressDetails[ADDRESS_DETAILS_AMOUNT_PLACEMENT_WITHOUT_CURRENCY_HASH]);
        Hash currencyDataHash = numOfDetailsInLine == NUMBER_OF_ADDRESS_LINE_DETAILS ? new Hash(addressDetails[CURRENCY_DATA_HASH_PLACEMENT]) : null;

        if (currencyDataHash != null) {
            baseNodeCurrencyService.verifyCurrencyExists(currencyDataHash);
            log.trace("The address hash {} for currency hash {} was loaded from the clusterstamp with amount {}", addressHash, currencyDataHash, addressAmount);
        } else {
            log.trace("The address hash {} was loaded from the clusterstamp with amount {}", addressHash, addressAmount);

            //TODO 8/27/2019 tomer: Consider treating this only as Native currency
            CurrencyData nativeCurrencyData = baseNodeCurrencyService.getNativeCurrencyData(); // Use this if needed
            if (nativeCurrencyData == null) {
                log.error("Failed to locate native currency");
                System.exit(SpringApplication.exit(applicationContext));
            }
        }

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