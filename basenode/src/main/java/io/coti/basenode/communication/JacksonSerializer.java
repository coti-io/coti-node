package io.coti.basenode.communication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.coti.basenode.communication.interfaces.ISerializer;
import io.coti.basenode.data.interfaces.IPropagatable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

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
            e.printStackTrace();
            return null;
        }
    }

    public <T extends IPropagatable> T deserialize(byte[] bytes) {
        try {
            return (T) serializer.readValue(bytes, IPropagatable.class);
        } catch (IOException e) {
            return null;
        }
    }
}
