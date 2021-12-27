package io.coti.zerospend.services;

import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Override
    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData) {
        clusterStampCrypto.signMessage(clusterStampData);
        updateClusterStampFileWithSignature(clusterStampData.getSignature());
    }

    private void updateClusterStampFileWithSignature(SignatureData signature) {
        String clusterstampFileLocation = clusterStampFolder + clusterStampFilePrefix + CLUSTERSTAMP_FILE_SUFFIX;
        try (FileWriter clusterstampFileWriter = new FileWriter(clusterstampFileLocation, true);
             BufferedWriter clusterStampBufferedWriter = new BufferedWriter(clusterstampFileWriter)) {
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("# Signature");
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("r,").append(signature.getR());
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("s,").append(signature.getS());
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
