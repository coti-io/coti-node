package io.coti.zerospend.services;

import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * A service that provides Cluster Stamp functionality for Zero Spend node.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Override
    protected void handleClusterStampWithoutSignature(ClusterStampData clusterStampData) throws Exception {
        clusterStampCrypto.signMessage(clusterStampData);
        updateClusterStampFileWithSignature(clusterStampData.getSignature());
    }

    private void updateClusterStampFileWithSignature(SignatureData signature) throws Exception {
        try {
            String clusterstampFileLocation = clusterStampFilePrefix + CLUSTERSTAMP_FILE_SUFFIX;
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
            e.printStackTrace();
            throw new Exception(BAD_CSV_FILE_FORMAT);
        }
    }

    @Override
    protected void setClusterStampSignerHash(ClusterStampData clusterStampData) {
        clusterStampData.setSignerHash(networkService.getNetworkNodeData().getNodeHash());
    }

}