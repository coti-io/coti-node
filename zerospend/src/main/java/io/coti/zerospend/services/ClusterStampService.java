package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.GeneralMessageCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.data.messages.StateMessageClusterStampExecutePayload;
import io.coti.basenode.data.messages.StateMessageClusterStampInitiatedPayload;
import io.coti.basenode.data.messages.StateMessageLastClusterStampIndexPayload;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.exceptions.FileSystemException;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.ClusterService;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IGeneralVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;


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
    @Autowired
    protected IPropagationPublisher propagationPublisher;
    @Autowired
    protected GeneralMessageCrypto generalMessageCrypto;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private IGeneralVoteService generalVoteService;
    private static final long CLUSTER_STAMP_INITIATED_DELAY = 100;
    private static final int NUMBER_OF_RESENDS = 3;
    private Thread clusterStampCreationThread;

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

    public ResponseEntity<IResponse> initiateClusterStamp() {
        StateMessageClusterStampInitiatedPayload stateMessageClusterstampInitiatedPayload = new StateMessageClusterStampInitiatedPayload(CLUSTER_STAMP_INITIATED_DELAY);
        StateMessage stateMessage = new StateMessage(stateMessageClusterstampInitiatedPayload);
        stateMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateMessage)));
        generalMessageCrypto.signMessage(stateMessage);
        propagationPublisher.propagate(stateMessage, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));
        log.info("Manually initiated clusterstamp" + stateMessage.getHash().toString());
        clusterStampCreationThread = new Thread(this::clusterStampCreation);
        clusterStampCreationThread.start();
        return ResponseEntity.ok().body(null);
    }

    private void clusterStampCreation() {
        try {
//            Thread.sleep(CLUSTER_STAMP_INITIATED_DELAY*900); // todo temporary shortened delay
            Thread.sleep(5000);
        } catch (Exception ignored) {
            // ignored exception
        }

        long lastConfirmedIndex = clusterService.maxIndexOfNotConfirmed();
        if (lastConfirmedIndex <= 0) {
            lastConfirmedIndex = transactionIndexService.getLastTransactionIndexData().getIndex();
        }

        StateMessageLastClusterStampIndexPayload stateMessageLastClusterStampIndexPayload = new StateMessageLastClusterStampIndexPayload(lastConfirmedIndex);
        StateMessage stateMessage = new StateMessage(stateMessageLastClusterStampIndexPayload);
        stateMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateMessage)));
        generalMessageCrypto.signMessage(stateMessage);
        generalVoteService.startCollectingVotes(stateMessage);
        log.info(String.format("Initiate voting for the last clusterstamp index %d %s", lastConfirmedIndex, stateMessage.getHash().toString()));
        propagationPublisher.propagate(stateMessage, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));

        for (int i = 0; i < NUMBER_OF_RESENDS; i++) {
            try {
                Thread.sleep(5000);
            } catch (Exception ignored) {
                // ignored exception
            }
            propagationPublisher.propagate(stateMessage, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));
        }

        // testexecution  todo delete it
        try {
            Thread.sleep(5000);
        } catch (Exception ignored) {
            // ignored exception
        }
        StateMessageClusterStampExecutePayload stateMessageClusterStampExecutePayload = new StateMessageClusterStampExecutePayload(stateMessage.getHash());
        StateMessage stateMessageExecute = new StateMessage(stateMessageClusterStampExecutePayload);
        stateMessageExecute.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateMessageExecute)));
        generalMessageCrypto.signMessage(stateMessageExecute);
        log.info("Initiate clusterstamp execution " + stateMessage.getHash().toString());
        propagationPublisher.propagate(stateMessageExecute, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));
        // end testexecution
    }

    protected void addVotesToClusterStamp(String clusterStampFileLocation, ClusterStampType clusterStampType) {
        GeneralVoteResult generalVoteResult = getVoteResult(clusterStampType);
        try (FileWriter clusterStampFileWriter = new FileWriter(clusterStampFileLocation, true);
             BufferedWriter clusterStampBufferedWriter = new BufferedWriter(clusterStampFileWriter)) {
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("# Votes");
            clusterStampBufferedWriter.newLine();
            for (GeneralVote generalVote : generalVoteResult.getHashToVoteMapping().values()) {
                writeGeneralVoteDetails(clusterStampBufferedWriter, generalVote);
            }
        } catch (Exception e) {
            throw new ClusterStampValidationException("Exception at clusterstamp signing.", e);
        }
    }

    private GeneralVoteResult getVoteResult(ClusterStampType clusterStampType) {
        return getGeneralVoteResult();
    }

    private void writeGeneralVoteDetails(BufferedWriter clusterStampBufferedWriter, GeneralVote generalVote) throws IOException {
        clusterStampBufferedWriter.append("k," + generalVote.getVoterHash());
        clusterStampBufferedWriter.newLine();
        clusterStampBufferedWriter.append("b," + generalVote.isVote());
        clusterStampBufferedWriter.newLine();
        clusterStampBufferedWriter.append("r," + generalVote.getSignature().getR());
        clusterStampBufferedWriter.newLine();
        clusterStampBufferedWriter.append("s," + generalVote.getSignature().getS());
        clusterStampBufferedWriter.newLine();
    }

    private void addVotesAndUploadCandidateClusterStamps(String candidateCurrencyClusterStampFileName, String candidateBalanceClusterStampFileName) {
        String currencyClusterStampFilename = clusterStampFolder + FOLDER_DELIMITER + candidateCurrencyClusterStampFileName;
        addVotesToClusterStamp(currencyClusterStampFilename, ClusterStampType.CURRENCY);
        uploadCandidateClusterStamp(candidateCurrencyClusterStampFileName);

        String balanceClusterStampFilename = clusterStampFolder + FOLDER_DELIMITER + candidateBalanceClusterStampFileName;
        addVotesToClusterStamp(balanceClusterStampFilename, ClusterStampType.BALANCE);
        uploadCandidateClusterStamp(candidateBalanceClusterStampFileName);
    }

    private void uploadCandidateClusterStamp(String candidateClusterStampFileName) {
        awsService.uploadFileToS3(candidateClusterStampBucketName, clusterStampFolder + candidateClusterStampFileName);
    }
}
