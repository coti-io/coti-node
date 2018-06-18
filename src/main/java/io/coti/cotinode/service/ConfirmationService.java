package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.model.Transaction;
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
    Logger log = LoggerFactory.getLogger(this.getClass().getName());
    ConcurrentHashMap<Hash, Transaction> hashToUnTddConfirmTransactionsMapping;
    private List<Pair> edges; // Adjacency List
    LinkedList<Transaction> result;



    ConcurrentHashMap<Hash, Transaction> hashToTddConfirmTransactionsMapping;
  //  List<LinkedList<Pair>> edges;


    public void init(ConcurrentHashMap<Hash, Transaction> hashToUnConfirmationTransactionsMapping) {
      for (Map.Entry<Hash, Transaction> entry : hashToUnConfirmationTransactionsMapping.entrySet()) {
          if (!entry.getValue().isTransactionConsensus()) {
              this.hashToUnTddConfirmTransactionsMapping.put( entry.getValue().getHash(),entry.getValue());
          }
      }
      initEdges();
    }

    @Async
    public Future<String> process() throws InterruptedException {
        log.info("###Start Processing with Thread id: " + Thread.currentThread().getId());

        // Sleep 3s for simulating the processing
        Thread.sleep(3000);

        String processInfo = String.format("Processing is Done with Thread id= %d", Thread.currentThread().getId());
        return new AsyncResult<>(processInfo);
    }

    public void initEdges() {
        edges = new Vector<Pair>();
        for (Transaction transaction : hashToUnTddConfirmTransactionsMapping.values()) {
            transaction.setTotalTrustScore(0);
            LinkedList<Transaction> childList =  new LinkedList<Transaction>();
            for (Hash childHash : transaction.getChildrenTransactions()) {
                childList.addFirst(hashToUnTddConfirmTransactionsMapping.get(childHash));
            }
            edges.add(new Pair(transaction, childList));
        }
    }

    public void findTransactionToconfirm() {

    }

    @Data
    public class Pair{
        Transaction parent;
        LinkedList<Transaction> childs;
        public Pair(Transaction parent,LinkedList<Transaction> childs){
            this.parent = parent;
            this.childs = childs;
        }
    }

    //takes adjacency list of a directed acyclic graph(DAG) as input
    //returns a linkedlist which consists the vertices in topological order
    public LinkedList<Transaction> topologicSorting(){

        HashSet< Transaction> visited =new HashSet< Transaction>();

        result=new LinkedList<Transaction>();
        //loop is for making sure that every vertex is visited since if we select only one random source
        //all vertices might not be reachable from this source
        //eg:1->2->3,1->3 and if we select 3 as source first then no vertex can be visited ofcourse except for 3
        for(Pair pair : edges){
            if(!visited.contains(pair.getParent()) ) {
                topoSortHelper(pair.parent, visited);
            }
        }
        return result;
    }

    private void topoSortHelper(Transaction parentTransaction, HashSet< Transaction> visited){
//        for(Hash hash : parentTransaction.getChildrenTransactions()){
//            //skipping through if already visited
//            if(!visited.contains(hashToUnTddConfirmTransactionsMapping.get(hash)) ) {
//                topoSortHelper(edges,adj.v,visited,result);
//            }
//
//        }
//        //making the vertex visited for future reference
//        visited[src]=true;
//        //pushing to the stack as departing
//        result.addFirst(src);
        
    }

}
