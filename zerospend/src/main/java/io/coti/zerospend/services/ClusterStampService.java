package io.coti.zerospend.services;

import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.exceptions.FileSystemException;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;


@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    private static final String CLUSTERSTAMP_DELIMITER = ",";
    @Value("${currency.genesis.address}")
    private String currencyAddress;
    @Value("${upload.clusterstamp}")
    private boolean uploadMajorClusterStamp;
    @Value("${upload.currencies.clusterstamp}")
    private boolean uploadCurrenciesClusterStamp;

    @Value("${aws.s3.bucket.name.clusterstamp}")
    private void setClusterStampBucketName(String clusterStampBucketName) {
        this.clusterStampBucketName = clusterStampBucketName;
    }

    @Override
    public void init() {
        super.init();
        if (uploadMajorClusterStamp) {
            uploadMajorClusterStamp();
        }
        if (uploadCurrenciesClusterStamp) {
            uploadCurrenciesClusterStamp();
        }
    }

    private void uploadCurrenciesClusterStamp() {
        log.info("Starting to upload currencies clusterstamp");
        uploadClusterStamp(currenciesClusterStampName);
        log.info("Finished to upload currencies clusterstamp");
    }

    private void uploadMajorClusterStamp() {
        log.info("Starting to upload major clusterstamp");
        uploadClusterStamp(majorClusterStampName);
        log.info("Finished to upload major clusterstamp");
    }

    private void uploadClusterStamp(ClusterStampNameData clusterStampNameData) {
        awsService.uploadFileToS3(clusterStampBucketName, clusterStampFolder + getClusterStampFileName(clusterStampNameData));
    }

    @Override
    protected void fillClusterStampNamesMap() {
        super.fillClusterStampNamesMap();

        long versionTimeInMillis = Instant.now().toEpochMilli();
        if (currenciesClusterStampName == null) {
            handleMissingCurrenciesClusterStamp(versionTimeInMillis);
        }
        if (majorClusterStampName == null) {
            handleMissingMajor(versionTimeInMillis);
        }
    }

    private void handleMissingMajor(long versionTimeInMillis) {
        CurrencyData nativeCurrency = currencyService.getNativeCurrency();
        if (nativeCurrency == null) {
            throw new ClusterStampException("Unable to start zero spend server. Native token not found.");
        }
        handleNewCurrencyByType(nativeCurrency, ClusterStampType.MAJOR, versionTimeInMillis);
        uploadMajorClusterStamp = true;
    }

    private void handleMissingCurrenciesClusterStamp(long versionTimeInMillis) {
        CurrencyData nativeCurrency = currencyService.getNativeCurrency();
        if (nativeCurrency == null) {
            currencyService.generateNativeCurrency();
            nativeCurrency = currencyService.getNativeCurrency();
        }
        generateCurrencyClusterStampFromNativeCurrency(nativeCurrency, versionTimeInMillis);
        uploadCurrenciesClusterStamp = true;
    }

    private ClusterStampNameData handleNewCurrencyByType(CurrencyData currency, ClusterStampType clusterStampType, long versionTimeInMillis) {
        ClusterStampNameData clusterStampNameData = new ClusterStampNameData(clusterStampType, versionTimeInMillis);
        generateOneLineClusterStampFile(clusterStampNameData, currency);
        addClusterStampName(clusterStampNameData);
        return clusterStampNameData;
    }

    private void generateOneLineClusterStampFile(ClusterStampNameData clusterStamp, CurrencyData currencyData) {
        String line = generateClusterStampLineFromNewCurrency(currencyData);
        fileSystemService.createAndWriteLineToFile(clusterStampFolder, super.getClusterStampFileName(clusterStamp), line);
    }

    private String generateClusterStampLineFromNewCurrency(CurrencyData currencyData) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.currencyAddress).append(CLUSTERSTAMP_DELIMITER).append(currencyData.getTotalSupply().toString()).append(CLUSTERSTAMP_DELIMITER).append(currencyData.getHash());
        return sb.toString();
    }

    private void generateCurrencyClusterStampFromNativeCurrency(CurrencyData nativeCurrency, long versionTimeInMillis) {
        if (currencyAddress == null) {
            throw new ClusterStampException("Unable to start zero spend server. Genesis address not found.");
        }
        if (nativeCurrency == null) {
            throw new ClusterStampException("Unable to start zero spend server. Native token not found.");
        }
        ClusterStampNameData clusterStampNameData = new ClusterStampNameData(ClusterStampType.CURRENCIES, versionTimeInMillis);
        String clusterStampFileName = getClusterStampFileName(clusterStampNameData);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(clusterStampFolder + "/" + clusterStampFileName))) {
            writer.write(CURRENCY_GENESIS_ADDRESS_HEADER);
            writer.newLine();
            writer.write(currencyAddress);
            writer.newLine();
            writer.write(CURRENCIES_DETAILS_HEADER);
            writer.newLine();
            writer.write(Base64.getEncoder().encodeToString(SerializationUtils.serialize(nativeCurrency)));
            writer.newLine();
            currenciesClusterStampName = validateNameAndGetClusterStampNameData(clusterStampFileName);
        } catch (IOException e) {
            throw new FileSystemException(String.format("Create and write file error. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    @Override
    protected void handleMissingRecoveryServer() {
        // Zero spend does nothing in this method.
    }

    @Override
    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData, String clusterstampFileLocation, boolean isMajor) {
        clusterStampCrypto.signMessage(clusterStampData);
        updateClusterStampFileWithSignature(clusterStampData.getSignature(), clusterstampFileLocation);
        if (isMajor) {
            uploadMajorClusterStamp = true;
        } else {
            uploadCurrenciesClusterStamp = true;
        }
    }

    private void updateClusterStampFileWithSignature(SignatureData signature, String clusterstampFileLocation) {
        try (FileWriter clusterstampFileWriter = new FileWriter(clusterstampFileLocation, true);
             BufferedWriter clusterStampBufferedWriter = new BufferedWriter(clusterstampFileWriter)) {
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("# Signature");
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("r," + signature.getR());
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("s," + signature.getS());
        } catch (Exception e) {
            throw new ClusterStampValidationException("Exception at clusterstamp signing.", e);
        }
    }

    @Override
    protected void setClusterStampSignerHash(ClusterStampData clusterStampData) {
        clusterStampData.setSignerHash(networkService.getNetworkNodeData().getNodeHash());
    }
}