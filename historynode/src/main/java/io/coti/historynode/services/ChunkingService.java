package io.coti.historynode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.exceptions.TransactionSyncException;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class ChunkingService {

    private final static long MAXIMUM_BUFFER_SIZE = 50000;

    @Autowired
    private JacksonSerializer jacksonSerializer;

    private ResponseExtractor getTransactionResponseExtractor(){
        log.info("Starting to get missing transactions");
        Map<Hash, TransactionData> bulkResponses = new HashMap<>(); //TODO 7/10/2019 astolia: should this be passed as argument??

        return response -> {
            byte[] buf = new byte[Math.toIntExact(MAXIMUM_BUFFER_SIZE)];
            int offset = 0;
            int n;
            while ((n = response.getBody().read(buf, offset, buf.length)) > 0) {
                try {
                    Pair<Hash, TransactionData> retrievedHashAndTransaciton = jacksonSerializer.deserialize(buf);
                    if (retrievedHashAndTransaciton != null) {
                        bulkResponses.put(retrievedHashAndTransaciton.getKey(),retrievedHashAndTransaciton.getValue());
//                        receivedMissingTransactionNumber.incrementAndGet();
//                        if (!insertMissingTransactionThread.isAlive()) {
//                            insertMissingTransactionThread.start();
//                        }
                        clearHandledDataFromBuf(buf, offset + n);
                        offset = 0;
                    } else {
                        offset += n;
                    }

                } catch (Exception e) {
                    throw new TransactionSyncException(e.getMessage());
                }
            }
            return null;
        };
//        restTemplate.execute(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_BATCH_ENDPOINT
//                + STARTING_INDEX_URL_PARAM_ENDPOINT + firstMissingTransactionIndex, HttpMethod.GET, null, responseExtractor);
    }

    private void clearHandledDataFromBuf(byte[] buf, int offset){
        Arrays.fill(buf, 0, offset, (byte) 0);
    }

    private Thread monitorTransactionThread(String type, AtomicLong transactionNumber, AtomicLong receivedTransactionNumber) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (receivedTransactionNumber != null) {
                    log.info("Received {} transactions: {}, inserted transactions: {}", type, receivedTransactionNumber, transactionNumber);
                } else {
                    log.info("Inserted {} transactions: {}", type, transactionNumber);
                }
            }
        });
    }
}
