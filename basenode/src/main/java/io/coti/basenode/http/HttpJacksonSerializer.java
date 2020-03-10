package io.coti.basenode.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.coti.basenode.http.interfaces.ISerializable;
import io.coti.basenode.http.interfaces.ISerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
@Component
public class HttpJacksonSerializer implements ISerializer {
    private ObjectMapper serializer;

    @PostConstruct
    private void init() {
        serializer = new ObjectMapper();
        serializer.registerModule(new JavaTimeModule());
        serializer.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public byte[] serialize(ISerializable entity) {
        try {
            return serializer.writeValueAsBytes(entity);
        } catch (JsonProcessingException e) {
            log.error("Error at jackson byte array serializer", e);
            return new byte[0];
        }
    }

    public <T extends ISerializable> T deserialize(byte[] bytes) {
        try {
            return (T) serializer.readValue(bytes, ISerializable.class);
        } catch (IOException e) {
            log.error("Error at jackson byte array deserializer", e);
            return null;
        }
    }
}
