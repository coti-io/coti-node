package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;

import io.coti.cotinode.data.TransactionData;
import lombok.Data;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConfirmationService {
    int TRESHOLD = 300;
    Logger log = LoggerFactory.getLogger(this.getClass().getName());
    ConcurrentHashMap<Hash, TransactionData> hashToUnTddConfirmTransactionsMapping;
    LinkedList<TransactionData>result;

    ConcurrentHashMap<Hash, TransactionData> hashToTddConfirmTransactionsMapping;

    public void init(ConcurrentHashMap<Hash, TransactionData> hashToUnConfirmationTransactionsMapping) {
      for (Map.Entry<Hash, TransactionData> entry : hashToUnConfirmationTransactionsMapping.entrySet()) {
          if (!entry.getValue().isTransactionConsensus()) {
              this.hashToUnTddConfirmTransactionsMapping.put( entry.getValue().getHash(),entry.getValue());
          }
      }
    }

    @Async
    public Future<String> process() throws InterruptedException {
        log.info("###Start Processing with Thread id: " + Thread.currentThread().getId());

        // Sleep 3s for simulating the processing
        Thread.sleep(3000);

        String processInfo = String.format("Processing is Done with Thread id= %d", Thread.currentThread().getId());
        return new AsyncResult<>(processInfo);
    }

    public void findTransactionToconfirm() {

    }

    //takes adjacency list of a directed acyclic graph(DAG) as input
    //returns a linkedlist which consists the vertices in topological order
    public LinkedList<TransactionData>topologicSorting(){

        result=new LinkedList<TransactionData>();

        //loop is for making sure that every vertex is visited since if we select only one random source
        //all vertices might not be reachable from this source
        //eg:1->2->3,1->3 and if we select 3 as source first then no vertex can be visited ofcourse except for 3
        for(Map.Entry<Hash, TransactionData>  entry : hashToUnTddConfirmTransactionsMapping.entrySet()){
            if(!entry.getValue().isVisit()) {
                topoSortHelper(entry.getValue());
            }
        }
        return result;
    }

    private void setMaxSonsTotalTrustScoregetMaxSonsTotalTrustScore(TransactionData parent) {
        int maxSonsTotalTrustScore = 0;
        Hash maxSonsTotalTrustScoreHash = null;
        for (Hash hash : parent.getChildrenTransactions()) {
            if (hashToUnTddConfirmTransactionsMapping.get(hash).getTotalTrustScore()
                    > maxSonsTotalTrustScore) {
                maxSonsTotalTrustScore = hashToUnTddConfirmTransactionsMapping.get(hash).getTotalTrustScore();
                maxSonsTotalTrustScoreHash = hash;
            }
        }

        //updating parent trustChainTransactionHashes
        List<Hash> maxSonsTotalTrustScoreTrustChainTransactionHashes =
                new Vector<>(hashToUnTddConfirmTransactionsMapping.get(maxSonsTotalTrustScoreHash).getTrustChainTransactionHashes());
        maxSonsTotalTrustScoreTrustChainTransactionHashes.add(maxSonsTotalTrustScoreHash);
        parent.setTrustChainTransactionHashes(maxSonsTotalTrustScoreTrustChainTransactionHashes);
    }

    private void topoSortHelper(TransactionData parentTransactionData){
        for(Hash transactionDataHash : parentTransactionData.getChildrenTransactions()){
            TransactionData childTransactionData = hashToUnTddConfirmTransactionsMapping.get(transactionDataHash);
            if(!childTransactionData.isVisit())  {
                topoSortHelper(childTransactionData);
            }

        }
        //making the vertex visited for future reference
        parentTransactionData.setVisit(true);

        //pushing to the stack as departing
        result.addLast(parentTransactionData);
    }

    private void setTransactionConsensus() {
        result.forEach((transaction) -> {
            setMaxSonsTotalTrustScoregetMaxSonsTotalTrustScore(transaction);
            if(transaction.getTotalTrustScore() >= TRESHOLD) {
                transaction.setTransactionConsensus(true);
            }
        });
    }

}
