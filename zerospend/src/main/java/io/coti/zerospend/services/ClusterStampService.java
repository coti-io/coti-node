package io.coti.zerospend.services;

import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.exceptions.FileSystemException;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;


@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    private static final String CLUSTERSTAMP_DELIMITER = ",";
    @Value("${currency.genesis.address}")
    private String currencyAddress;
    @Value("${upload.clusterstamp}")
    private boolean uploadClusterStamp;
    @Value("${upload.currencies.clusterstamp}")
    private boolean uploadCurrencyClusterStamp;

    @Value("${aws.s3.bucket.name.clusterstamp}")
    private void setClusterStampBucketName(String clusterStampBucketName) {
        this.clusterStampBucketName = clusterStampBucketName;
    }

    @Override
    public void init() {
        super.init();
        if (uploadClusterStamp) {
            uploadCurrencyClusterStamp();
            uploadBalanceClusterStamp();
        }
    }

    private void uploadCurrencyClusterStamp() {
        log.info("Starting to upload currency clusterstamp");
        uploadClusterStamp(currencyClusterStampName);
        log.info("Finished to upload currency clusterstamp");

    }

    private void uploadBalanceClusterStamp() {
        log.info("Starting to upload balance clusterstamp");
        uploadClusterStamp(balanceClusterStampName);
        log.info("Finished to upload balance clusterstamp");
    }

    private void uploadClusterStamp(ClusterStampNameData clusterStampNameData) {
        awsService.uploadFileToS3(clusterStampBucketName, clusterStampFolder + getClusterStampFileName(clusterStampNameData));
    }

    @Override
    protected void fillClusterStampNamesMap() {
        super.fillClusterStampNamesMap();

        long versionTimeInMillis = Instant.now().toEpochMilli();
        if (currencyClusterStampName == null) {
            handleMissingCurrencyClusterStamp(versionTimeInMillis);
        }
        if (balanceClusterStampName == null) {
            handleMissingBalanceClusterStamp(versionTimeInMillis);
        }
    }

    private void handleMissingCurrencyClusterStamp(long versionTimeInMillis) {
        CurrencyData nativeCurrency = currencyService.getNativeCurrency();
        if (nativeCurrency == null) {
            currencyService.generateNativeCurrency();
            nativeCurrency = currencyService.getNativeCurrency();
        }
        generateCurrencyClusterStampFromNativeCurrency(nativeCurrency, versionTimeInMillis);
        uploadClusterStamp = true;
    }

    private void handleMissingBalanceClusterStamp(long versionTimeInMillis) {
        CurrencyData nativeCurrency = currencyService.getNativeCurrency();
        if (nativeCurrency == null) {
            throw new ClusterStampException("Unable to start zero spend server. Native token not found.");
        }
        handleNewCurrencyByType(nativeCurrency, ClusterStampType.BALANCE, versionTimeInMillis);
        uploadClusterStamp = true;
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
        ClusterStampNameData clusterStampNameData = new ClusterStampNameData(ClusterStampType.CURRENCY, versionTimeInMillis);
        String clusterStampFileName = getClusterStampFileName(clusterStampNameData);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(clusterStampFolder + "/" + clusterStampFileName))) {
            writeNativeCurrencyDetails(nativeCurrency, writer, currencyAddress);
            addClusterStampName(clusterStampNameData);
        } catch (IOException e) {
            throw new FileSystemException(String.format("Create and write file error. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    @Override
    protected void handleMissingRecoveryServer() {
        // Zero spend does nothing in this method.
    }

    @Override
    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData, String clusterStampFileLocation) {
        clusterStampCrypto.signMessage(clusterStampData);
        updateClusterStampFileWithSignature(clusterStampData.getSignature(), clusterStampFileLocation);
        uploadClusterStamp = true;
    }

    private void updateClusterStampFileWithSignature(SignatureData signature, String clusterStampFileLocation) {
        try (FileWriter clusterStampFileWriter = new FileWriter(clusterStampFileLocation, true);
             BufferedWriter clusterStampBufferedWriter = new BufferedWriter(clusterStampFileWriter)) {
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