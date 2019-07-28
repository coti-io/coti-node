package io.coti.storagenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.GetHashToTransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ChunkingService {

    @Autowired
    private JacksonSerializer jacksonSerializer;

    public void getTransactionBatch(HttpServletResponse response) {
        Map<Hash, TransactionData> hashToTransactionData = new HashMap<>(); //TODO 7/10/2019 astolia: get this from somewhere.
        try {
            ServletOutputStream output = response.getOutputStream();
            for (Map.Entry<Hash, TransactionData> entry : hashToTransactionData.entrySet()) {
                //TODO 7/10/2019 astolia: not sure about the  'GetHashToTransactionResponse' solution.
                output.write(jacksonSerializer.serialize(new GetHashToTransactionData(entry.getKey(), entry.getValue())));
                output.flush();
            }
        } catch (Exception e) {
            log.error("Error sending transaction batch");
            log.error(e.getMessage());
        }
    }

    public void getTransaction(GetHashToTransactionData entry, ServletOutputStream output) {
        try {
            output.write(jacksonSerializer.serialize(entry));
            output.flush();
        } catch (Exception e) {
            log.error("Error sending transaction batch");
            log.error(e.getMessage());
        }
    }

}



