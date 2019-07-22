package io.coti.basenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.ChunkException;
import io.coti.basenode.services.interfaces.IChunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;

import java.util.Arrays;
import java.util.function.Consumer;

@Service
public class BaseNodeChunkService implements IChunkService {

    @Autowired
    private JacksonSerializer jacksonSerializer;

    public ResponseExtractor getResponseExtractor(Consumer<IPropagatable> consumer, int maxBufferSize) {
        return response -> {
            byte[] buf = new byte[maxBufferSize];
            int offset = 0;
            int n;
            while ((n = response.getBody().read(buf, offset, buf.length)) > 0) {
                try {
                    IPropagatable chunkedData = jacksonSerializer.deserialize(buf);
                    if (chunkedData != null) {
                        consumer.accept(chunkedData);
                        Arrays.fill(buf, 0, offset + n, (byte) 0);
                        offset = 0;
                    } else {
                        offset += n;
                    }
                } catch (Exception e) {
                    throw new ChunkException(e.getMessage());
                }
            }
            return null;
        };
    }
}
