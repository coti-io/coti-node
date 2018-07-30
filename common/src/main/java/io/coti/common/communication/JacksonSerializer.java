package io.coti.common.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.data.interfaces.IEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class JacksonSerializer implements ISerializer {
    private ObjectMapper serializer;

    @PostConstruct
    private void init() {
        serializer = new ObjectMapper();
        serializer.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public byte[] serialize(IEntity entity) {
        try {
            return serializer.writeValueAsBytes(entity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T extends IEntity> T deserialize(byte[] bytes) {
        try {
            return (T) serializer.readValue(bytes, IEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
