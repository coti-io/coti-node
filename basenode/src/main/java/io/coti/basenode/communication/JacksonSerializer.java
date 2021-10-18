package io.coti.basenode.communication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Service
public class JacksonSerializer implements ISerializer {

    private ObjectMapper serializer;

    @PostConstruct
    private void init() {
        serializer = new ObjectMapper();
        serializer.registerModule(new JavaTimeModule());
        serializer.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        serializer.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public byte[] serialize(IPropagatable entity) {
        try {
            return serializer.writeValueAsBytes(entity);
        } catch (JsonProcessingException e) {
            log.error("Error at jackson byte array serializer", e);
            return new byte[0];
        }
    }

    @Override
    public String serializeAsString(IPropagatable entity) {
        try {
            return serializer.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.error("Error at jackson serializer", e);
            return null;
        }
    }

    public IPropagatable deserialize(byte[] bytes) {
        try {
            return serializer.readValue(bytes, IPropagatable.class);
        } catch (IOException e) {
            return null;
        }
    }

    public <T extends IPropagatable> T deserialize(String string) {
        try {
            return (T) serializer.readValue(string, IPropagatable.class);
        } catch (IOException e) {
            return null;
        }
    }
}
