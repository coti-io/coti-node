package io.coti.basenode.services;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.exceptions.ChunkException;
import io.coti.basenode.http.CustomGson;
import io.coti.basenode.http.data.CurrencyResponseData;
import io.coti.basenode.http.data.GetHashToPropagatable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Service
@Slf4j
public class BaseNodeCurrencyChunkService extends BaseNodeChunkService {

    private static final int MAXIMUM_BUFFER_SIZE = 50000;

    public void currencyHandler(Consumer<ResponseExtractor> extractorConsumer, PrintWriter output) {
        try {
            AtomicBoolean firstCurrencyArrived = new AtomicBoolean(false);

            extractorConsumer.accept(super.getResponseExtractor(chunkedData -> {
                try {
                    GetHashToPropagatable<CurrencyData> getHashToCurrencyData = (GetHashToPropagatable<CurrencyData>) chunkedData;
                    CurrencyData currencyData = getHashToCurrencyData.getData();
                    if (currencyData != null) {
                        if (currencyData.getHash().equals(getHashToCurrencyData.getHash())) {
                            if (firstCurrencyArrived.get() == true) {
                                sendChunk(",", output);
                            } else {
                                firstCurrencyArrived.set(true);
                            }
                            currencyHandler(currencyData, output);
                        } else {
                            log.error("Mismatched transactionHashes {}, {}", currencyData.getHash(), getHashToCurrencyData.getHash());
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

    public void currencyHandler(CurrencyData currencyData, PrintWriter output) {
        try {
            sendChunk(new CustomGson().getInstance().toJson(new CurrencyResponseData(currencyData)), output);
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }
    }

}
