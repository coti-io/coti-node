package io.coti.zerospend.services;

import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;


@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Value("${currency.genesis.address}")
    private String currencyAddress;
    @Value("${upload.clusterstamp}")
    private boolean uploadMajorClusterStamp;

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
    }

    private void uploadMajorClusterStamp() {
        log.info("Starting to upload major clusterstamp");
        uploadClusterStamp(majorClusterStampName);
        log.info("Finished to upload major clusterstamp");
    }

    private void uploadClusterStamp(ClusterStampNameData clusterStampNameData) {
        awsService.uploadFileToS3(clusterStampBucketName, clusterStampFolder + getClusterStampFileName(clusterStampNameData));
    }

    protected void fillClusterStampNamesMap() {
        super.fillClusterStampNamesMap();
        if (majorClusterStampName == null) {
            handleMissingMajor();
        }

    }

    private void handleMissingMajor() {
        CurrencyData nativeCurrency = currencyService.getNativeCurrency();
        if (nativeCurrency == null) {
            throw new ClusterStampException("Unable to start zero spend server. Native token not found.");
        }
        handleNewCurrencyByType(nativeCurrency, ClusterStampType.MAJOR);
        uploadMajorClusterStamp = true;
    }

    private ClusterStampNameData handleNewCurrencyByType(CurrencyData currency, ClusterStampType clusterStampType) {
        ClusterStampNameData clusterStampNameData = new ClusterStampNameData(clusterStampType);
        generateOneLineClusterStampFile(clusterStampNameData, currency);
        addClusterStampName(clusterStampNameData);
        return clusterStampNameData;
    }

    private void generateOneLineClusterStampFile(ClusterStampNameData clusterStamp, CurrencyData currencyData) {
        String line = generateClusterStampLineFromNewCurrency(currencyData);
        fileSystemService.createAndWriteLineToFile(clusterStampFolder, super.getClusterStampFileName(clusterStamp), line);

    }

    private String generateClusterStampLineFromNewCurrency(CurrencyData currencyData) {
        String clusterStampDelimiter = ",";
        StringBuilder sb = new StringBuilder();
        sb.append(this.currencyAddress).append(clusterStampDelimiter).append(currencyData.getTotalSupply().toString()).append(clusterStampDelimiter).append(currencyData.getHash());
        return sb.toString();
    }

    @Override
    protected void handleMissingRecoveryServer() {
        // Zero spend does nothing in this method.
    }

    @Override
    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData, String clusterstampFileLocation) {
        clusterStampCrypto.signMessage(clusterStampData);
        updateClusterStampFileWithSignature(clusterStampData.getSignature(), clusterstampFileLocation);
        uploadMajorClusterStamp = true;
    }

    private void updateClusterStampFileWithSignature(SignatureData signature, String clusterstampFileLocation) {
        try (FileWriter clusterstampFileWriter = new FileWriter(clusterstampFileLocation, true);
             BufferedWriter clusterStampBufferedWriter = new BufferedWriter(clusterstampFileWriter)) {
            clusterStampBufferedWriter.newLine();
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

    public ClusterStampNameData handleNewToken(CurrencyData currencyData) {
        ClusterStampNameData clusterStampNameData = handleNewCurrencyByType(currencyData, ClusterStampType.TOKEN);
        loadClusterStamp(clusterStampNameData);
        uploadClusterStamp(clusterStampNameData);
        return clusterStampNameData;
    }
}