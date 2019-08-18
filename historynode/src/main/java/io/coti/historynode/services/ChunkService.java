package io.coti.historynode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.exceptions.ChunkException;
import io.coti.basenode.http.CustomGson;
import io.coti.basenode.http.data.GetHashToPropagatable;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.services.BaseNodeChunkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Service
@Slf4j
public class ChunkService extends BaseNodeChunkService {

    private static final int MAXIMUM_BUFFER_SIZE = 50000;

    public void transactionHandler(Consumer<ResponseExtractor> extractorConsumer, PrintWriter output) {
        try {
            AtomicBoolean firstTransactionArrived = new AtomicBoolean(false);

            extractorConsumer.accept(super.getResponseExtractor(chunkedData -> {
                try {
                    GetHashToPropagatable<TransactionData> getHashToTransactionData = (GetHashToPropagatable<TransactionData>) chunkedData;
                    TransactionData transactionData = getHashToTransactionData.getData();
                    if (transactionData != null) {
                        if (transactionData.getHash().equals(getHashToTransactionData.getHash())) {
                            if (firstTransactionArrived.get() == true) {
                                sendChunk(",", output);
                            } else {
                                firstTransactionArrived.set(true);
                            }
                            transactionHandler(transactionData, output);
                        } else {
                            log.error("Mismatched transactionHashes {}, {}", transactionData.getHash(), getHashToTransactionData.getHash());
                        }
                    }
                } catch (Exception e) {
                    throw new ChunkException(e.getMessage());
                }
            }, MAXIMUM_BUFFER_SIZE));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }

    }

    public void transactionHandler(TransactionData transactionData, PrintWriter output) {
        try {
            sendChunk(new CustomGson().getInstance().toJson(new TransactionResponseData(transactionData)), output);
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }
    }

}
