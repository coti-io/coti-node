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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;

@Service
@Slf4j
public class ChunkService extends BaseNodeChunkService {

    private final static int MAXIMUM_BUFFER_SIZE = 50000;

    public void startOfChunk(HttpServletResponse response) {
        try {
            sendChunk("[", response.getWriter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endOfChunk(HttpServletResponse response) {
        try {
            sendChunk("]", response.getWriter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendChunk(String string, PrintWriter output) {
        output.write(string);
        output.flush();
    }

    public void transactionHandler(Consumer<ResponseExtractor> extractorConsumer, HttpServletResponse response) {
        try {
            PrintWriter output = response.getWriter();

            extractorConsumer.accept(super.getResponseExtractor(chunkedData -> {
                try {
                    GetHashToPropagatable<TransactionData> getHashToTransactionData = (GetHashToPropagatable<TransactionData>) chunkedData;
                    TransactionData transactionData = getHashToTransactionData.getData();
                    if (transactionData != null) {
                        if (transactionData.getHash().equals(getHashToTransactionData.getHash())) {
                            sendChunk(",", output);
                            sendChunk(new CustomGson().getInstance().toJson(new TransactionResponseData(transactionData)), output);
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

    public void transactionHandler(TransactionData transactionData, HttpServletResponse response) {
        try {
            PrintWriter output = response.getWriter();
            sendChunk(new CustomGson().getInstance().toJson(new TransactionResponseData(transactionData)), output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
