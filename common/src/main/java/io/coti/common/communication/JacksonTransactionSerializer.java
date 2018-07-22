package io.coti.common.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.common.communication.interfaces.ITransactionSerializer;
import io.coti.common.data.TransactionData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class JacksonTransactionSerializer implements ITransactionSerializer {
    private ObjectMapper serializer;
    @PostConstruct
    private void init(){
        serializer = new ObjectMapper();
        serializer.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public byte[] serializeTransaction(TransactionData transactionData) {
        try {
            return serializer.writeValueAsBytes(transactionData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public TransactionData deserializeMessage(byte[] bytes) {
        try {
            return serializer.readValue(bytes, TransactionData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
