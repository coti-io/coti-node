package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.ClusterStamps;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * An abstract class that provides basic Cluster Stamp functionality for all nodes that take part in cluster stamp flow.
 */
@Slf4j
@Service
public class BaseNodeClusterStampService implements IClusterStampService {

    protected static final Hash LAST_CLUSTER_STAMP_HASH = new Hash(0);

    public static final String CLUSTERSTAMP_FILE_SUFFIX = "_clusterStamp.csv";

    private static final int NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES = 3; // Genesis One and Two + heading
    public static final int NUMBER_OF_ADDRESS_LINE_DETAILS = 2;
    public static final int ADDRESS_DETAILS_HASH_PLACEMENT = 0;
    public static final int ADDRESS_DETAILS_AMOUNT_PLACEMENT = 1;
    public static final String BAD_CSV_FILE_FORMAT = "Bad csv file format";
    public static final String BALANCE_ADDRESSES_LINE_TOKEN = "# Balance Addresses";
    public static final String SIGNATURE_LINE_TOKEN = "# Signature";
    public static final int NUMBER_OF_SIGNATURE_LINE_DETAILS = 2;


    @Value("${logging.file.name}")
    private String clusterStampFilePrefix;

    @Autowired
    protected IPropagationPublisher propagationPublisher;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    protected TccConfirmationService tccConfirmationService;
    @Autowired
    protected Transactions transactions;
    @Autowired
    protected ClusterStamps clusterStamps;
    @Autowired
    protected ClusterStampCrypto clusterStampCrypto;
    @Autowired
    protected INetworkService networkService;


    protected Map<Hash, TransactionData> getUnconfirmedTransactions() {
        return new HashMap<>();
    }

    @Override
    public void loadBalanceFromLastClusterStamp() {

        ClusterStampData clusterStampData = getLastClusterStamp();

        if(clusterStampData != null) { // TODO: assumes BalanceMap and UnconfirmedTransactions may be present in clusterStampData
            loadBalanceFromClusterStamp(clusterStampData);
        }
    }

    protected void loadBalanceFromClusterStamp(ClusterStampData clusterStampData) {
        //TODO: disable use of balanceService
//        balanceService.updateBalanceAndPreBalanceMap(clusterStampData.getBalanceMap());
        transactions.deleteAll();
        Iterator it = clusterStampData.getUnconfirmedTransactions().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry unConfirmedTransaction = (Map.Entry)it.next();
            transactions.put( (TransactionData)unConfirmedTransaction.getValue() );
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public ClusterStampData getNewerClusterStamp(long totalConfirmedTransactionsPriorClusterStamp) {
        ClusterStampData lastClusterStampData = getLastClusterStamp();
        if(lastClusterStampData.getTotalConfirmedTransactionsPriorClusterStamp() > totalConfirmedTransactionsPriorClusterStamp) {
            return lastClusterStampData;
        }
        return null;
    }

    public SignatureData getNewerClusterStampSignature(long totalConfirmedTransactionsPriorClusterStamp) {
        ClusterStampData lastClusterStampData = getNewerClusterStamp(totalConfirmedTransactionsPriorClusterStamp);
        if(lastClusterStampData != null) {
            return lastClusterStampData.getSignature();
        }
        return null;
    }

    public Hash getSignerHash(long totalConfirmedTransactionsPriorClusterStamp) {
        ClusterStampData lastClusterStampData = getNewerClusterStamp(totalConfirmedTransactionsPriorClusterStamp);
        if(lastClusterStampData != null) {
            return lastClusterStampData.getZeroSpendHash();
        }
        return null;
    }

    public File getNewerClusterStampFile() {
        String clusterStampFileLocation = clusterStampFilePrefix+CLUSTERSTAMP_FILE_SUFFIX;
        File clusterStampFile = new File(clusterStampFileLocation);
        return clusterStampFile;

    }

    protected ClusterStampData getLastClusterStamp() {

        long totalConfirmedTransactionsPriorClusterStamp;
        boolean matchingSignatures = false;
        ClusterStampData lastClusterStampData = null;
        Hash zeroSpendHash = null;
        RestTemplate restTemplate = new RestTemplate();

        ClusterStampData localClusterStampData = clusterStamps.getByHash(LAST_CLUSTER_STAMP_HASH);
        if (localClusterStampData != null) {
            totalConfirmedTransactionsPriorClusterStamp = localClusterStampData.getTotalConfirmedTransactionsPriorClusterStamp();
        }
        else {
            totalConfirmedTransactionsPriorClusterStamp = -1;
        }

        String recoveryServerAddress = networkService.getRecoveryServerAddress();
        if( recoveryServerAddress!=null && !recoveryServerAddress.isEmpty() )
        {
            Path destFile= Paths.get(clusterStampFilePrefix+CLUSTERSTAMP_FILE_SUFFIX);
            matchingSignatures = isMatchingSignaturesOfClusterStampFiles(totalConfirmedTransactionsPriorClusterStamp, matchingSignatures, restTemplate);

            if(!matchingSignatures)
            {
                //copyClusterStampFileFromRecovery();

                // TODO: get cluster stamp data from recovery server
//                ResponseEntity<byte[]> response =
//                        restTemplate.postForEntity(recoveryServerAddress + "/getClusterStampFile",null ,byte[].class);

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
                HttpEntity<String> entity = new HttpEntity<String>(headers);
                ResponseEntity<byte[]> response =
                        restTemplate.exchange(recoveryServerAddress + "/getClusterStampFile", HttpMethod.GET, entity, byte[].class);

                if(response.getStatusCodeValue() == HttpServletResponse.SC_OK)
                {
                    try {
                        Files.write(destFile, response.getBody()); //TODO: write as a temp file until signature is verified
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


                // TODO: copy the file from recovery, locally


            }
            // If signatures do match or after retrieving file from recovery, create clusterStampData from local file.
            lastClusterStampData = loadInitialClusterStamp();
            //TODO: Switch to ger Signer Hash of Recovery Server from NetworkService, networkService.getRecoveryServerHash
            if(lastClusterStampData != null && lastClusterStampData.getZeroSpendHash()==null) //TODO: needed for signature checks
            {
                zeroSpendHash = getZeroSpendHash(totalConfirmedTransactionsPriorClusterStamp);
                lastClusterStampData.setSignerHash(zeroSpendHash);
            }

//            // TODO: temp checks - start
//            ClusterStampData lastClusterStampDataNotFile =
//                        restTemplate.postForObject(
//                                recoveryServerAddress + "/getLastClusterStamp",
//                                totalConfirmedTransactionsPriorClusterStamp,
//                                ClusterStampData.class);
//            boolean verifyClusterStampDataNotFile = clusterStampCrypto.verifySignature(lastClusterStampDataNotFile);
//            // TODO: temp checks - end

            //TODO: currently verifySignature is failing
            if(lastClusterStampData != null && clusterStampCrypto.verifySignature(lastClusterStampData) ) {
                clusterStamps.put(lastClusterStampData);
                log.info("Received last cluster stamp for total confirmed transactions: {}", totalConfirmedTransactionsPriorClusterStamp);
                return lastClusterStampData;
            }
        }
        return localClusterStampData;
    }

    @Override
    public void getClusterStampFile(HttpServletResponse response) throws IOException {
        try {
            String clusterStampFileLocation = clusterStampFilePrefix+CLUSTERSTAMP_FILE_SUFFIX;
            File localFile = new File(clusterStampFileLocation);
            InputStream inputStream = new FileInputStream(localFile);
            response.setStatus(HttpServletResponse.SC_OK);
            FileCopyUtils.copy(inputStream, response.getOutputStream());
            inputStream.close();

        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            response.getWriter().write("Document not found");
        }
    }

    private boolean isMatchingSignaturesOfClusterStampFiles(long totalConfirmedTransactionsPriorClusterStamp, boolean matchingSignatures, RestTemplate restTemplate) {
        // Check for presence of local cluster stamp file "clusterStampFilePrefix_clusterStamp.csv"
        // If file is present and signed, compare signature with that of the recovery server cluster stamp
        String localClusterStampFileName = clusterStampFilePrefix+CLUSTERSTAMP_FILE_SUFFIX;
        SignatureData potentialSignatureFromLocalFile = null;
        try {
            potentialSignatureFromLocalFile = getPotentialSignatureFromFile(localClusterStampFileName);
        } catch (IOException e) {
            // null or unsigned file is acceptable
            log.info("Signature from local cluster stamp file was not found");
        }
        if(potentialSignatureFromLocalFile != null) {
            SignatureData lastClusterStampDataSignature =
                    restTemplate.postForObject(
                            networkService.getRecoveryServerAddress() + "/getLastClusterStampSignature",
                            totalConfirmedTransactionsPriorClusterStamp,
                            SignatureData.class);
            if (lastClusterStampDataSignature != null)
                matchingSignatures = lastClusterStampDataSignature.equals(potentialSignatureFromLocalFile);
        }
        return matchingSignatures;
    }

    private Hash getZeroSpendHash(long totalConfirmedTransactionsPriorClusterStamp) {
        RestTemplate restTemplate = new RestTemplate();
        Hash signerHash = restTemplate.postForObject(
                networkService.getRecoveryServerAddress() + "/getSignerHash",
                totalConfirmedTransactionsPriorClusterStamp,
                Hash.class);
        return signerHash;
    }

    private void copyClusterStampFileFromRecovery() {
        //TODO: instead of getting the object, get the whole file.
        //TODO: until the end point passing the actual file is implemented, grab the file of zeroSpend directly

//        deletePreviousClusterStampFile();

        Path srcFile = Paths.get("ZeroSpendServer_clusterStamp.csv");
        Path destFile= Paths.get(clusterStampFilePrefix+CLUSTERSTAMP_FILE_SUFFIX);
        try {
            Files.copy(srcFile, destFile, REPLACE_EXISTING);

        } catch (IOException e) {
            e.printStackTrace();
        }
//                File fileRecovered = restTemplate.postForObject(
//                        recoveryServerAddress + "/getLastClusterStampFile", null,
//                        File.class);
//
//                byte[] fff = (byte[]) Files.readAllBytes(requireNonNull(fileRecovered))
//                Path path = (Path) Files.write(clusterStampFilePrefix + CLUSTERSTAMP_FILE_SUFFIX, fff, StandardOpenOption.CREATE);

// Originally would have received the clusterStamp object and not as a file
//                lastClusterStampData =
//                        restTemplate.postForObject(
//                                recoveryServerAddress + "/getLastClusterStamp",
//                                totalConfirmedTransactionsPriorClusterStamp,
//                                ClusterStampData.class);
    }


    private SignatureData getPotentialSignatureFromFile(String fileLocation) throws IOException {
        SignatureData signature = null;
        Stream<String> linesStream = null;

        try {
            linesStream = Files.newBufferedReader(Paths.get(fileLocation)).lines();
            long numberOfLinesInFile = linesStream.count();
            String[] signatureDetails;

            if(numberOfLinesInFile > 3)
            {
                List<String> lastLinesFromFileList = null;
                lastLinesFromFileList = Files.newBufferedReader(Paths.get(fileLocation)).lines().skip(numberOfLinesInFile - 3).collect(Collectors.toList());
                if(lastLinesFromFileList.get(0).contentEquals(SIGNATURE_LINE_TOKEN))
                {
                    signatureDetails = lastLinesFromFileList.get(1).split(",");
                    if(signatureDetails.length != NUMBER_OF_SIGNATURE_LINE_DETAILS) {
                        return null;
                    } else {
                        if(!signatureDetails[0].equalsIgnoreCase("r"))
                            return null;
                        else
                        {
                            signature = new SignatureData();
                            signature.setR(signatureDetails[1]);
                            signatureDetails = lastLinesFromFileList.get(2).split(",");
                            if(signatureDetails.length != NUMBER_OF_SIGNATURE_LINE_DETAILS) {
                                return null;
                            } else {
                                if(!signatureDetails[0].equalsIgnoreCase("s"))
                                    return null;
                                else
                                {
                                    signature.setS(signatureDetails[1]);
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            if(linesStream!=null)
                linesStream.close();
        }
        return signature;
    }


    public ClusterStampData loadInitialClusterStamp() {
        String clusterstampFileLocation = clusterStampFilePrefix+CLUSTERSTAMP_FILE_SUFFIX;
        File clusterstampFile = new File(clusterstampFileLocation);
        ClusterStampData initialClusterStampData = new ClusterStampData();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(clusterstampFile))) {
            String line;
            int relevantLineNumber = 0;
            int signatureRelevantLines = 0;
            boolean reachedSignatureSection = false;
            boolean finishedInitialAddresses = false;
            boolean encounteredEmptyLine = false;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                relevantLineNumber++;
                if (relevantLineNumber == 1) {
                    if (!line.contentEquals(BALANCE_ADDRESSES_LINE_TOKEN))
                        throw new Exception(BAD_CSV_FILE_FORMAT);
                    continue;
                }
                if (line.isEmpty()) {
                    encounteredEmptyLine = true;
                    if (relevantLineNumber < NUMBER_OF_GENESIS_ADDRESSES_MIN_LINES) {
                        throw new Exception(BAD_CSV_FILE_FORMAT);
                    } else {
                        if (!finishedInitialAddresses)
                            finishedInitialAddresses = true;
                        else
                            throw new Exception(BAD_CSV_FILE_FORMAT);
                    }
                } else {
                    if (!finishedInitialAddresses) {
                        fillGenesisAddressesInitialClusterStampDataFromLine(initialClusterStampData, line);
                    } else {
                        if (!reachedSignatureSection) {
                            if (!line.contentEquals(SIGNATURE_LINE_TOKEN))
                                throw new Exception(BAD_CSV_FILE_FORMAT);
                            else
                                reachedSignatureSection = true;
                        } else {
                            signatureRelevantLines++;
                            fillSignatureDataFromLine(initialClusterStampData, line, signatureRelevantLines);
                        }

                    }
                }
            }
            if(encounteredEmptyLine && signatureRelevantLines==0)
                throw new Exception(BAD_CSV_FILE_FORMAT);

            clusterStamps.put(initialClusterStampData);
            log.info("Initial clusterStamp is loaded");
        } catch (Exception e) {
            log.error("Errors on clusterStamp loading: {}", e);
        }
        return initialClusterStampData;
    }

    private void fillGenesisAddressesInitialClusterStampDataFromLine(ClusterStampData initialClusterStampData, String line) throws Exception {
        String[] addressDetails;
        addressDetails = line.split(",");
        if (addressDetails.length != NUMBER_OF_ADDRESS_LINE_DETAILS) {
            throw new Exception(BAD_CSV_FILE_FORMAT);
        }
        Hash addressHash = new Hash(addressDetails[ADDRESS_DETAILS_HASH_PLACEMENT]);
        BigDecimal addressAmount = new BigDecimal(addressDetails[ADDRESS_DETAILS_AMOUNT_PLACEMENT]);
//        initialClusterStampData.getBalanceMap().put(addressHash, addressAmount);
//        initialClusterStampData.getBalanceMapHashes().add(addressHash);
//        initialClusterStampData.getBalanceMapAmounts().add(addressAmount);

        ByteArrayOutputStream baos = initialClusterStampData.getBaosRowsBytes(); // TODO: Improve this
        byte[] hashInBytes = addressHash.getBytes();
        byte[] addressAmountInBytes = addressAmount.toString().getBytes();
        baos.write(hashInBytes);
        baos.write(addressAmountInBytes);
//        initialClusterStampData.setBaosRowsBytes( baos );
    }

    private void fillSignatureDataFromLine(ClusterStampData initialClusterStampData, String line, int signatureRelevantLines) throws Exception {
        if(signatureRelevantLines>2) { throw new Exception(BAD_CSV_FILE_FORMAT); }

        String[] signatureDetails;
        signatureDetails = line.split(",");
        if(signatureDetails.length != NUMBER_OF_SIGNATURE_LINE_DETAILS) {
            throw new Exception(BAD_CSV_FILE_FORMAT);
        }
        String signaturePrefix = (signatureRelevantLines==1) ? "r" : "s";
        if(!signatureDetails[0].equalsIgnoreCase(signaturePrefix)) {
            throw new Exception(BAD_CSV_FILE_FORMAT);
        }

        if(signatureRelevantLines==1) {
            SignatureData signature = new SignatureData();
            initialClusterStampData.setSignature(signature);
            initialClusterStampData.getSignature().setR(signatureDetails[1]);
        }
        else
            initialClusterStampData.getSignature().setS(signatureDetails[1]);
    }

    public void updateClusterStampFileWithSignature(SignatureData signature) {
        String clusterstampFileLocation = clusterStampFilePrefix+CLUSTERSTAMP_FILE_SUFFIX;
        FileWriter clusterstampFileWriter;// = new File(clusterstampFileLocation);
        try
        {
            clusterstampFileWriter = new FileWriter(clusterstampFileLocation, true);
            BufferedWriter clusterStampBufferedWriter = new BufferedWriter(clusterstampFileWriter);
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("# Signature");
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("r,"+signature.getR());
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("s,"+signature.getS());
            clusterStampBufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}