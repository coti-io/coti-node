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
import java.io.IOException;


@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Value("${native.token.genesis.address}")
    private String nativeTokenAddress;

    @Override
    public boolean init() {
        boolean uploadMajorToS3AfterLoad = super.init();
        if (uploadMajorToS3AfterLoad) {
            uploadMajorClusterStamp();
        }
        lastClusterStampVersions.put(new LastClusterStampVersionData(majorClusterStampName.getVersionTimeMillis()));
        return false;
    }

    private void uploadMajorClusterStamp() {
        awsService.uploadFileToS3(clusterStampBucketName, clusterStampsFolder + getClusterStampFileName(majorClusterStampName));
    }

    @Override
    protected boolean handleMissingMajor() {
        CurrencyData nativeCurrency = currencyService.getNativeCurrency();
        if (nativeCurrency == null) {
            throw new ClusterStampException("Unable to start zero spend server. Native token not found.");
        }
        ClusterStampNameData nativeMajorClusterStamp = new ClusterStampNameData(ClusterStampType.MAJOR);
        generateOneLineClusterStampFile(nativeMajorClusterStamp, nativeCurrency);
        addClusterStampName(nativeMajorClusterStamp);
        return true;
    }

    private void generateOneLineClusterStampFile(ClusterStampNameData clusterStamp, CurrencyData currencyData) {
        String line = generateClusterStampLineFromNewCurrency(currencyData);
        fileSystemService.createAndWriteLineToFile(clusterStampsFolder, super.getClusterStampFileName(clusterStamp), line);

    }

    private String generateClusterStampLineFromNewCurrency(CurrencyData currencyData) {
        String clusterStampDelimiter = ",";
        StringBuilder sb = new StringBuilder();
        sb.append(nativeTokenAddress).append(clusterStampDelimiter).append(currencyData.getHash()).append(clusterStampDelimiter).append(currencyData.getTotalSupply().toString());
        return sb.toString();
    }

    @Override
    protected void handleMissingRecoveryServer() {
        // Zero spend does nothing in this case.
    }

    @Override
    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData, String clusterstampFileLocation) {
        clusterStampCrypto.signMessage(clusterStampData);
        updateClusterStampFileWithSignature(clusterStampData.getSignature(), clusterstampFileLocation);
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
        } catch (IOException e) {
            log.error("Exception at clusterstamp signing");
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }
    }

    @Override
    protected void setClusterStampSignerHash(ClusterStampData clusterStampData) {
        clusterStampData.setSignerHash(networkService.getNetworkNodeData().getNodeHash());
    }

}