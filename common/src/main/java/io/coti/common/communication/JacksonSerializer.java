package io.coti.common.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class JacksonSerializer implements ISerializer {
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
    public TransactionData deserializeTransaction(byte[] bytes) {
        try {
            return serializer.readValue(bytes, TransactionData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] serializeDspVote(DspVote dspVote) {
        try {
            return serializer.writeValueAsBytes(dspVote);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public DspVote deserializeDspVote(byte[] bytes) {
        try {
            return serializer.readValue(bytes, DspVote.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] serializeAddress(AddressData addressData) {
        try {
            return serializer.writeValueAsBytes(addressData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public AddressData deserializeAddress(byte[] bytes) {
        try {
            return serializer.readValue(bytes, AddressData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }    }
}
