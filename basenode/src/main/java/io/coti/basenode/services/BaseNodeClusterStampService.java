package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.basenode.services.BaseNodeServiceManager.*;

@Slf4j
@Service
public class BaseNodeClusterStampService implements IClusterStampService {

    protected static final String CLUSTERSTAMP_FILE_SUFFIX = "_clusterstamp.csv";
    private static final int NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES = 1; // Genesis One and Two + heading
    private static final int DETAILS_IN_CLUSTERSTAMP_LINE_WITHOUT_CURRENCY_HASH = 2;
    private static final int DETAILS_IN_CLUSTERSTAMP_LINE_WITH_CURRENCY_HASH = 3;
    private static final int ADDRESS_HASH_INDEX_IN_CLUSTERSTAMP_LINE = 0;
    private static final int AMOUNT_INDEX_IN_CLUSTERSTAMP_LINE = 1;
    private static final int CURRENCY_HASH_INDEX_IN_CLUSTERSTAMP_LINE = 2;
    protected static final String BAD_CSV_FILE_FORMAT = "Bad csv file format";
    private static final String SIGNATURE_LINE_TOKEN = "# Signature";
    private static final int NUMBER_OF_SIGNATURE_LINE_DETAILS = 2;
    @Value("${logging.file.name}")
    protected String clusterStampFilePrefix;
    @Value("${data.path:./}")
    protected String clusterStampFolder;

    @Override
    public void init() {
        loadClusterStamp();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void loadClusterStamp() {
        String clusterStampFileLocation = clusterStampFolder + clusterStampFilePrefix + CLUSTERSTAMP_FILE_SUFFIX;
        File clusterStampFile = new File(clusterStampFileLocation);
        ClusterStampData clusterStampData = new ClusterStampData();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(clusterStampFile))) {
            String line;
            AtomicInteger relevantLineNumber = new AtomicInteger(0);
            AtomicInteger signatureRelevantLines = new AtomicInteger(0);
            AtomicBoolean reachedSignatureSection = new AtomicBoolean(false);
            AtomicBoolean finishedBalances = new AtomicBoolean(false);

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                relevantLineNumber.incrementAndGet();
                if (line.isEmpty()) {
                    handleEmptyLine(relevantLineNumber, finishedBalances);
                } else {
                    fillDataFromLine(clusterStampData, line, signatureRelevantLines, reachedSignatureSection, finishedBalances);
                }
            }
            if (signatureRelevantLines.get() == 0) {
                handleClusterStampWithoutSignature(clusterStampData);
            } else if (signatureRelevantLines.get() == 1) {
                throw new ClusterStampValidationException("Signature lines can not be a single line at clusterstamp file");
            } else {
                handleClusterStampWithSignature(clusterStampData);
            }
        } catch (ClusterStampException e) {
            throw new ClusterStampException("Errors on balance clusterstamp file loading.%n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException("Errors on balance clusterstamp file loading.", e);
        }
    }

    private void handleEmptyLine(AtomicInteger relevantLineNumber, AtomicBoolean finishedBalances) {
        if (relevantLineNumber.get() < NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        } else {
            if (!finishedBalances.get()) {
                finishedBalances.set(true);
            } else {
                throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
            }
        }
    }

    private void fillDataFromLine(ClusterStampData clusterStampData, String line, AtomicInteger signatureRelevantLines, AtomicBoolean reachedSignatureSection, AtomicBoolean finishedBalances) {
        if (!finishedBalances.get()) {
            fillBalanceFromLine(clusterStampData, line);
        } else {
            if (!reachedSignatureSection.get()) {
                if (!line.contentEquals(SIGNATURE_LINE_TOKEN)) {
                    throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
                } else {
                    reachedSignatureSection.set(true);
                }
            } else {
                signatureRelevantLines.incrementAndGet();
                fillSignatureDataFromLine(clusterStampData, line, signatureRelevantLines);
            }
        }
    }

    private void fillBalanceFromLine(ClusterStampData clusterStampData, String line) {
        String[] lineDetails = line.split(",");
        int numOfDetailsInLine = lineDetails.length;
        if (numOfDetailsInLine != DETAILS_IN_CLUSTERSTAMP_LINE_WITH_CURRENCY_HASH && numOfDetailsInLine != DETAILS_IN_CLUSTERSTAMP_LINE_WITHOUT_CURRENCY_HASH) {
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }
        Hash addressHash = new Hash(lineDetails[ADDRESS_HASH_INDEX_IN_CLUSTERSTAMP_LINE]);
        BigDecimal currencyAmountInAddress = new BigDecimal(lineDetails[AMOUNT_INDEX_IN_CLUSTERSTAMP_LINE]);
        Hash currencyHash = numOfDetailsInLine == DETAILS_IN_CLUSTERSTAMP_LINE_WITH_CURRENCY_HASH && !lineDetails[CURRENCY_HASH_INDEX_IN_CLUSTERSTAMP_LINE].isEmpty() ? new Hash(lineDetails[CURRENCY_HASH_INDEX_IN_CLUSTERSTAMP_LINE]) : null;
        currencyHash = currencyService.getNativeCurrencyHashIfNull(currencyHash);

        balanceService.updateBalanceAndPreBalanceFromClusterStamp(addressHash, currencyHash, currencyAmountInAddress);
        log.trace("The address hash {} for currency hash {} was loaded from the clusterstamp with amount {}", addressHash, currencyHash, currencyAmountInAddress);

        byte[] addressHashInBytes = addressHash.getBytes();
        byte[] addressCurrencyAmountInBytes = currencyAmountInAddress.stripTrailingZeros().toPlainString().getBytes();
        byte[] currencyHashInBytes = numOfDetailsInLine == DETAILS_IN_CLUSTERSTAMP_LINE_WITH_CURRENCY_HASH ? currencyHash.getBytes() : new byte[0];
        byte[] balanceInBytes = ByteBuffer.allocate(addressHashInBytes.length + addressCurrencyAmountInBytes.length + currencyHashInBytes.length)
                .put(addressHashInBytes).put(addressCurrencyAmountInBytes).put(currencyHashInBytes).array();
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
        } else {
            clusterStampData.getSignature().setS(signatureDetails[1]);
        }
    }

    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData) {
        throw new ClusterStampValidationException("ClusterStamp doesn't contain signature");
    }

    private void handleClusterStampWithSignature(ClusterStampData clusterStampData) {
        setClusterStampSignerHash(clusterStampData);
        if (!clusterStampCrypto.verifySignature(clusterStampData)) {
            throw new ClusterStampValidationException("Clusterstamp invalid signature");
        }
    }

    protected void setClusterStampSignerHash(ClusterStampData clusterStampData) {
        Optional<NetworkNodeData> optionalZeroSpendServer = Optional.ofNullable(networkService.getSingleNodeData(NodeType.ZeroSpendServer));
        if (!optionalZeroSpendServer.isPresent()) {
            throw new ClusterStampValidationException("ZeroSpend server doesn't run");
        }
        clusterStampData.setSignerHash(optionalZeroSpendServer.get().getNodeHash());
    }

}
