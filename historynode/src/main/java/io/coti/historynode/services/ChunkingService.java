package io.coti.historynode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.exceptions.TransactionSyncException;
import io.coti.basenode.http.GetHashToTransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class ChunkingService {

    private final static long MAXIMUM_BUFFER_SIZE = 50000;

    @Autowired
    private JacksonSerializer jacksonSerializer;

    protected ResponseExtractor getTransactionResponseExtractor(){
        log.info("Starting to get stored transactions");
        List<GetHashToTransactionResponse> bulkResponses = new ArrayList<>();

        return response -> {
            byte[] buf = new byte[Math.toIntExact(MAXIMUM_BUFFER_SIZE)];
            int offset = 0;
            int n;
            while ((n = response.getBody().read(buf, offset, buf.length)) > 0) {
                try {
                    GetHashToTransactionResponse retrievedHashAndTransaction = jacksonSerializer.deserialize(buf);
                    if (retrievedHashAndTransaction != null) {
                        //TODO 7/21/2019 tomer: introduce logic for cases with missing transaction data
                        TransactionData transactionData = retrievedHashAndTransaction.getTransactionData();
                        if (transactionData != null) {
                            if(transactionData.getHash().equals(retrievedHashAndTransaction.getHash()))
                            {
                                //TODO 7/22/2019 tomer: Find how to access bulkResponses outside of extractor or change accordingly
                                bulkResponses.add(retrievedHashAndTransaction);
                                //TODO 7/10/2019 astolia: handled arrived data here
                                log.info(retrievedHashAndTransaction.getHash().toString());
                                log.info(retrievedHashAndTransaction.getTransactionData().toString());
                            } else {
                                log.error("Mismatched hashes {}, {}",transactionData.getHash(), retrievedHashAndTransaction.getHash());
                            }
                        }

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
