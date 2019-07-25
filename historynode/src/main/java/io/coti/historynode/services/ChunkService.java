package io.coti.historynode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.CustomGson;
import io.coti.basenode.http.data.GetHashToTransactionData;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.services.BaseNodeChunkService;
import io.coti.historynode.data.AddressMissingTransactionsByHash;
import io.coti.historynode.model.AddressMissingTransactionsByHashes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.function.Consumer;

@Service
@Slf4j
public class ChunkService extends BaseNodeChunkService {

    private final static int MAXIMUM_BUFFER_SIZE = 50000;

    @Autowired
    private AddressMissingTransactionsByHashes addressMissingTransactionsByHashes;

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
                    GetHashToTransactionData getHashToTransactionData = (GetHashToTransactionData) chunkedData;
                    TransactionData transactionData = getHashToTransactionData.getTransactionData();
                    if (transactionData != null) {
                        if (transactionData.getHash().equals(getHashToTransactionData.getHash())) {
                            sendChunk(",", output);
                            sendChunk(new CustomGson().getInstance().toJson(new TransactionResponseData(transactionData)), output);
                        } else {
                            log.error("Mismatched hashes {}, {}", transactionData.getHash(), getHashToTransactionData.getHash());
                        }
                    } else {
                        Hash missingTransactionHash = getHashToTransactionData.getHash();
                        log.error("Missing transaction from storage {}", missingTransactionHash);
                        AddressMissingTransactionsByHash addressMissingTransactionsByHash = addressMissingTransactionsByHashes.getByHash(missingTransactionHash);
                        if (addressMissingTransactionsByHash != null) {
                            addressMissingTransactionsByHash.setLastTimeEncountered(Instant.now());
                        } else {
                            addressMissingTransactionsByHashes.put(new AddressMissingTransactionsByHash(getHashToTransactionData.getHash(), Instant.now(), "Missing", Instant.now()));
                        }
                    }
                } catch (Exception e) {

                }
            }, MAXIMUM_BUFFER_SIZE));
        } catch (IOException e) {
            e.printStackTrace();
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
