package io.coti.zerospend.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * A service that provides Cluster Stamp functionality for Zero Spend node.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    public static final String CLUSTERSTAMP_FILE_SUFFIX = "_clusterStamp.csv";

    @Value("${logging.file.name}")
    private String clusterStampFilePrefix;


    @Override
    protected ClusterStampData getLastClusterStamp() {
        ClusterStampData localClusterStampData = clusterStamps.getByHash(LAST_CLUSTER_STAMP_HASH);
        boolean writeSignatureToFile = false;
        ClusterStampData loadedClusterStampData = null;
        if(localClusterStampData == null) {
            loadedClusterStampData = loadInitialClusterStamp();
            writeSignatureToFile = !(loadedClusterStampData!= null && loadedClusterStampData.getSignature()!= null);
            localClusterStampData = clusterStamps.getByHash(LAST_CLUSTER_STAMP_HASH);
        }

        clusterStampCrypto.signMessage(localClusterStampData);
        if(writeSignatureToFile)
        {
            updateClusterStampFileWithSignature(localClusterStampData.getSignature());
        }
        return localClusterStampData;
    }

//    public void getClusterStampFile(HttpServletResponse response) throws IOException {
//        try {
//            String clusterStampFileLocation = clusterStampFilePrefix+CLUSTERSTAMP_FILE_SUFFIX;
//            File localFile = new File(clusterStampFileLocation);
//            InputStream inputStream = new FileInputStream(localFile);
////            byte[] inputStreamAsBytes = IOUtils.toByteArray(inputStream);
//            response.setStatus(HttpServletResponse.SC_OK);
//            FileCopyUtils.copy(inputStream, response.getOutputStream());
//            inputStream.close();
//
//        } catch (IOException e) {
//            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
//            response.getWriter().write("Document not found");
//        }
//
//    }
}