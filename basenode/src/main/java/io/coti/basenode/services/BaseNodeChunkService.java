package io.coti.basenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.ChunkException;
import io.coti.basenode.services.interfaces.IChunkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.function.Consumer;

@Service
@Slf4j
public class BaseNodeChunkService implements IChunkService {

    @Autowired
    private JacksonSerializer jacksonSerializer;

    @Override
    public void startOfChunk(PrintWriter output) {
        sendChunk("[", output);
    }

    @Override
    public void endOfChunk(PrintWriter output) {
        sendChunk("]", output);
    }

    @Override
    public void sendChunk(String string, PrintWriter output) {
        try {
            output.write(string);
            output.flush();
        } catch (Exception e) {
            throw new ChunkException(e.getMessage());
        }
    }

    @Override
    public ResponseExtractor getResponseExtractor(Consumer<IPropagatable> consumer, int maxBufferSize) {
        return response -> {
            try {
                byte[] buf = new byte[maxBufferSize];
                int offset = 0;
                int n;
                while ((n = response.getBody().read(buf, offset, buf.length - offset)) > 0) {
                    IPropagatable chunkedData = jacksonSerializer.deserialize(buf);
                    if (chunkedData != null) {
                        consumer.accept(chunkedData);
                        Arrays.fill(buf, 0, offset + n, (byte) 0);
                        offset = 0;
                    } else {
                        offset += n;
                    }
                }
                return null;
            } catch (Exception e) {
                throw new ChunkException(e.getMessage());
            }
        };
    }
}
