package io.coti.zerospend.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A service that provides Cluster Stamp functionality for Zero Spend node.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

//    private static final int NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES = 3; // Genesis One and Two + heading
//    public static final int NUMBER_OF_ADDRESS_LINE_DETAILS = 2;
//    public static final int ADDRESS_DETAILS_HASH_PLACEMENT = 0;
//    public static final int ADDRESS_DETAILS_AMOUNT_PLACEMENT = 1;
//    public static final String BAD_CSV_FILE_FORMAT = "Bad csv file format";
//    public static final String BALANCE_ADDRESSES_LINE_TOKEN = "# Balance Addresses";
//    public static final String SIGNATURE_LINE_TOKEN = "# Signature";
//    public static final int NUMBER_OF_SIGNATURE_LINE_DETAILS = 2;
//    public static final int SIGNATURE_RELEVANT_LINES_AMOUNT = 3;
//    public static final String CLUSTERSTAMP_FILE_LOCATION = "clusterstamp.csv";
    public static final String CLUSTERSTAMP_FILE_SUFFIX = "_clusterStamp.csv";

    @Value("${logging.file.name}")
    private String clusterStampFilePrefix;

//    private boolean loadInitialClusterStamp() {
//
//        String clusterstampFileLocation = CLUSTERSTAMP_FILE_LOCATION;  //TODO: consider changing to property
//        File clusterstampFile = new File(clusterstampFileLocation);
//        ClusterStampData initialClusterStampData = new ClusterStampData();
//        boolean readSignatureFromFile = false;
//
//        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(clusterstampFile))) {
//            String line;
//            int relevantLineNumber = 0;
//            int signatureRelevantLines = 0;
//            boolean reachedSignatureSection = false;
//            boolean finishedInitialAddresses = false;
//            boolean encounteredEmptyLine = false;
//
//            while ((line = bufferedReader.readLine()) != null) {
//                line = line.trim();
//                relevantLineNumber++;
//                if (relevantLineNumber == 1) {
//                    if (!line.contentEquals(BALANCE_ADDRESSES_LINE_TOKEN))
//                        throw new Exception(BAD_CSV_FILE_FORMAT);
//                    continue;
//                }
//                if (line.isEmpty()) {
//                    encounteredEmptyLine = true;
//                    if (relevantLineNumber < NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES) {
//                        throw new Exception(BAD_CSV_FILE_FORMAT);
//                    } else {
//                        if (!finishedInitialAddresses)
//                            finishedInitialAddresses = true;
//                        else
//                            throw new Exception(BAD_CSV_FILE_FORMAT);
//                    }
//                } else {
//                    if (!finishedInitialAddresses) {
//                        fillGenesisAddressesInitialClusterStampDataFromLine(initialClusterStampData, line);
//                    } else {
//                        if (!reachedSignatureSection) {
//                            if (!line.contentEquals(SIGNATURE_LINE_TOKEN))
//                                throw new Exception(BAD_CSV_FILE_FORMAT);
//                            else
//                                reachedSignatureSection = true;
//                        } else {
//                            signatureRelevantLines++;
//                            fillSignatureDataFromLine(initialClusterStampData, line, signatureRelevantLines);
//                        }
//
//                    }
//                }
//            }
//            if(encounteredEmptyLine && signatureRelevantLines==0)
//                throw new Exception(BAD_CSV_FILE_FORMAT);
//
//            clusterStamps.put(initialClusterStampData);
//            log.info("Initial clusterstamp is loaded");
//            readSignatureFromFile = (signatureRelevantLines == SIGNATURE_RELEVANT_LINES_AMOUNT-1);
//        } catch (Exception e) {
//            log.error("Errors on clusterstamp loading: {}", e);
//        }
//        return readSignatureFromFile;
//    }



//    private void fillGenesisAddressesInitialClusterStampDataFromLine(ClusterStampData initialClusterStampData, String line) throws Exception {
//        String[] addressDetails;
//        addressDetails = line.split(",");
//        if (addressDetails.length != NUMBER_OF_ADDRESS_LINE_DETAILS) {
//            throw new Exception(BAD_CSV_FILE_FORMAT);
//        }
//        Hash addressHash = new Hash(addressDetails[ADDRESS_DETAILS_HASH_PLACEMENT]);
//        BigDecimal addressAmount = new BigDecimal(addressDetails[ADDRESS_DETAILS_AMOUNT_PLACEMENT]);
//        initialClusterStampData.getBalanceMap().put(addressHash, addressAmount);
//    }
//
//    private void fillSignatureDataFromLine(ClusterStampData initialClusterStampData, String line, int signatureRelevantLines) throws Exception {
//        if(signatureRelevantLines>2) { throw new Exception(BAD_CSV_FILE_FORMAT); }
//
//        String[] signatureDetails;
//        signatureDetails = line.split(",");
//        if(signatureDetails.length != NUMBER_OF_SIGNATURE_LINE_DETAILS) {
//            throw new Exception(BAD_CSV_FILE_FORMAT);
//        }
//        String signaturePrefix = (signatureRelevantLines==1) ? "r" : "s";
//        if(!signatureDetails[0].equalsIgnoreCase(signaturePrefix)) {
//            throw new Exception(BAD_CSV_FILE_FORMAT);
//        }
//
//        if(signatureRelevantLines==1) {
//            SignatureData signature = new SignatureData();
//            initialClusterStampData.setSignature(signature);
//            initialClusterStampData.getSignature().setR(signatureDetails[1]);
//        }
//        else
//            initialClusterStampData.getSignature().setS(signatureDetails[1]);
//    }



    @Override
    protected ClusterStampData getLastClusterStamp() {
        ClusterStampData localClusterStampData = clusterStamps.getByHash(LAST_CLUSTER_STAMP_HASH);
        boolean writeSignatureToFile = false;
        ClusterStampData loadedClusterStampData = null;
        if(localClusterStampData == null) {
            loadedClusterStampData = loadInitialClusterStamp();
            writeSignatureToFile = !(loadedClusterStampData!= null && loadedClusterStampData.getSignature()!= null);
//            writeSignatureToFile = !loadInitialClusterStamp();
            localClusterStampData = clusterStamps.getByHash(LAST_CLUSTER_STAMP_HASH);
        }

        clusterStampCrypto.signMessage(localClusterStampData);
        if(writeSignatureToFile)
        {
            updateClusterStampFileWithSignature(localClusterStampData.getSignature());
        }
        return localClusterStampData;
    }

//    private void updateClusterStampFileWithSignature(SignatureData signature) {
//        String clusterstampFileLocation = clusterStampFilePrefix+CLUSTERSTAMP_FILE_SUFFIX;
//        FileWriter clusterstampFileWriter;// = new File(clusterstampFileLocation);
//        try
//        {
//            clusterstampFileWriter = new FileWriter(clusterstampFileLocation, true);
//            BufferedWriter clusterStampBufferedWriter = new BufferedWriter(clusterstampFileWriter);
//
//            clusterStampBufferedWriter.newLine();
//            clusterStampBufferedWriter.newLine();
//            clusterStampBufferedWriter.append("# Signature");
//            clusterStampBufferedWriter.newLine();
//
//            clusterStampBufferedWriter.append("r,"+signature.getR());
//            clusterStampBufferedWriter.newLine();
//
//            clusterStampBufferedWriter.append("s,"+signature.getS());
//
//            clusterStampBufferedWriter.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
}