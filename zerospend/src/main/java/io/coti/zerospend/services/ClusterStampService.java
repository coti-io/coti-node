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
    protected void handleMissingMajor(){
        //TODO 9/9/2019 astolia: implement
        CurrencyData nativeCurrency = currencyService.getNativeCurrency();
        //check if there is a native token in the db.
        if(nativeCurrency == null){
            //if not - exception
            throw new ClusterStampException("Unable to start zero spend server. Native token not found.");
        }
        // if there isn't - create a clusterstamp major file
        ClusterStampNameData nativeMajorClusterStamp = new ClusterStampNameData(ClusterStampType.MAJOR);
        generateOneLineClusterStampFile(nativeMajorClusterStamp, nativeCurrency);
        addClusterStampName(nativeMajorClusterStamp);
        //super.loadClusterStamp(clusterStampsFolder, nativeMajorClusterStamp);
        // upload the file to s3
        // set the major field
        // set the version if it isn't defined.

        //throw new ClusterStampException("Unable to start zero spend server. Major clusterstamp not found.");
    }

    private void generateOneLineClusterStampFile(ClusterStampNameData clusterStamp, CurrencyData currencyData){
        String line = generateClusterStampLineFromNewCurrency(currencyData);
        fileSystemService.createAndWriteLineToFile(clusterStampsFolder, super.getClusterStampFileName(clusterStamp), line);
        //fileSystemService.createFile(clusterStampsFolder, super.getClusterStampFileName(clusterStamp));


    }

    private String generateClusterStampLineFromNewCurrency(CurrencyData currencyData){
        String clusterStampDelimiter = ",";
        StringBuilder sb = new StringBuilder();
        sb.append(nativeTokenAddress).append(clusterStampDelimiter).append(currencyData.getHash()).append(clusterStampDelimiter).append(currencyData.getTotalSupply().toString());
        return sb.toString();
    }

    @Override
    protected void handleMissingRecoveryServer(){
        // Zero spend does nothing in this case.
    }

    @Override
    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData, String clusterstampFileLocation) {
        //TODO 9/10/2019 astolia: make sure the sum of balances is equal to total supply from properties before signing

        clusterStampCrypto.signMessage(clusterStampData);
        updateClusterStampFileWithSignature(clusterStampData.getSignature(), clusterstampFileLocation);
    }

    private void updateClusterStampFileWithSignature(SignatureData signature, String clusterstampFileLocation) {
        try {
            FileWriter clusterstampFileWriter = new FileWriter(clusterstampFileLocation, true);
            BufferedWriter clusterStampBufferedWriter = new BufferedWriter(clusterstampFileWriter);
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("# Signature");
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("r," + signature.getR());
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("s," + signature.getS());
            clusterStampBufferedWriter.close();
        } catch (IOException e) {
            log.error("Exception at clusterstamp signing");
            throw new ClusterStampValidationException(BAD_CSV_FILE_FORMAT);
        }
    }

    @Override
    protected void setClusterStampSignerHash(ClusterStampData clusterStampData) {
        clusterStampData.setSignerHash(networkService.getNetworkNodeData().getNodeHash());
    }

    @Override
    public void generateNativeTokenClusterStamp() {

    }

}