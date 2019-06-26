package io.coti.storagenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.storagenode.services.interfaces.IHistoryNodesConsensusService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.*;

@Deprecated
@Service
@Slf4j
@Data
public class HistoryNodesConsensusService implements IHistoryNodesConsensusService {


//    @Autowired
//    private HistoryNodeConsensusCrypto historyNodeConsensusCrypto;

//    private Map<Hash, PublicKey> historyNodesPublicKeys;
//    private List<Hash> liveHistoryNodes;


    public void init() {
//        historyNodesPublicKeys = new HashMap<>();
//        liveHistoryNodes = new ArrayList<>();
    }


    @Override
    public ResponseEntity<IResponse> validateStoreMultipleObjectsConsensus(Map<Hash, String> hashToObjectAsJsonStringMap)
    {
//        ResponseEntity response = verifyMasterNodeSignature(historyNodeConsensusResult);
        // TODO: Commented section until Consensus is filled and can be verified
//        if( !isResponseOK(response) )
//            return response;
//
//        byte[] originalDataToVerify = hashToObjectAsJsonStringMap.toString().getBytes();
//        return verifyHistoryNodesVotes(historyNodeConsensusResult, response, originalDataToVerify);
        return null;
    }


    @Deprecated
    @Override
    public ResponseEntity<IResponse> validateRetrieveMultipleObjectsConsensus(List<Hash> hashes)
    {
//        ResponseEntity response = verifyMasterNodeSignature(historyNodeConsensusResult);
        // TODO: Commented section until Consensus is filled and can be verified
//        if( !isResponseOK(response) )
//            return response;
//
//        byte[] originalDataToVerify = hashes.toString().getBytes();
//        return verifyHistoryNodesVotes(historyNodeConsensusResult, response, originalDataToVerify);
        return null;
    }


//    private ResponseEntity verifyMasterNodeSignature(HistoryNodeConsensusResult historyNodeConsensusResult) {
//        ResponseEntity response = ResponseEntity.status(HttpStatus.OK).body( "All is well");
//        // TODO: Commented section until Consensus is filled and can be verified
//        // Verify Master node signature
///*        if( !historyNodeConsensusCrypto.verifySignature(historyNodeConsensusResult) )
//        {
//            response = ResponseEntity
//                    .status(HttpStatus.NOT_ACCEPTABLE)
//                    .body( "History Master node signature failed verification");
//            log.error("History Master node signature failed verification");
//        }*/
//        return response;
//    }


//    private ResponseEntity<IResponse> verifyHistoryNodesVotes(HistoryNodeConsensusResult historyNodeConsensusResult, ResponseEntity response, byte[] originalDataToVerify) {
//        // Verify each history node's signature from consensus
//        List<HistoryNodeVote> historyNodesVotesList = historyNodeConsensusResult.getHistoryNodesVotesList();
//        List<HistoryNodeVote> confirmedHistoryNodesList=null;
//        if( historyNodesVotesList!= null && !historyNodesVotesList.isEmpty() )
//        {
//            confirmedHistoryNodesList = historyNodesVotesList.stream().filter(hVote -> {
//                        try {
//                            if( !isVoteByLiveHistoryNode(hVote) )
//                                return false;
//                            return CryptoHelper.VerifyByPublicKey(originalDataToVerify, hVote.getSignature().getR(),
//                                    hVote.getSignature().getS(), hVote.getSignerHash().toHexString()) && hVote.isValidRequest(); // TODO verify if checks are enough
//                        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
//                            e.printStackTrace();
//                            return false;
//                        }
//                    }
//            ).collect(Collectors.toList());
//        }
//
//        //TODO
//        if ( confirmedHistoryNodesList!= null && !isMajorityReached( confirmedHistoryNodesList ) )
//            response = ResponseEntity
//                    .status(HttpStatus.NOT_ACCEPTABLE)
//                    .body("Suggested consensus failed to meet majority requirement.");
//        return response;
//    }



//    private boolean isResponseOK(ResponseEntity<IResponse> iResponse) {
//        return iResponse != null && iResponse.getStatusCode().equals(HttpStatus.OK);
//    }

//    private boolean isMajorityReached(List<HistoryNodeVote> confirmedHistoryNodesList)
//    {
//        return true; // TODO remove once liveliness indication is provided
//        // TODO compare confirmed votes with existing list history nodes
////        long liveHistoryNodesAmount = liveHistoryNodes.stream().distinct().count();
////        long confirmedByHistoryNodesAmount = confirmedHistoryNodesList.stream().distinct().count();
////        final boolean majorityReached = confirmedByHistoryNodesAmount > (liveHistoryNodesAmount / 2);
////        return majorityReached;
//    }

//    private boolean isVoteByLiveHistoryNode(HistoryNodeVote hVote)
//    {
//        return true; // TODO remove once liveliness indication is provided
//        // TODO assumes hVote.getSignerHash() returns node's public key?
//        // Get entries for nodes from expected public key in history vote and verify the node is alive
////        Set<Hash> collectNodesBySignature = historyNodesPublicKeys.entrySet().stream().filter(entry ->
////                Objects.equals(entry.getValue(), hVote.getSignerHash())).map(Map.Entry::getKey).collect(Collectors.toSet());
////        return  liveHistoryNodes.containsAll(collectNodesBySignature);
//    }

//    private PublicKey getHistoryNodesMasterPublicKey() {
//        return null; // TODO
//    }

}

