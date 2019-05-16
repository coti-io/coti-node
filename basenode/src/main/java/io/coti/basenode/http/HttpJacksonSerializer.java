package io.coti.basenode.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.coti.basenode.http.interfaces.ISerializable;
import io.coti.basenode.http.interfaces.ISerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

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
            e.printStackTrace();
            return null;
        }
    }

    public <T extends ISerializable> T deserialize(byte[] bytes) {
        try {
            return (T) serializer.readValue(bytes, ISerializable.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
