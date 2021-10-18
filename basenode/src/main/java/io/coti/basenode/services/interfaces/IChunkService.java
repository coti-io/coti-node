package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.interfaces.IPropagatable;
import org.springframework.web.client.ResponseExtractor;

import java.io.PrintWriter;
import java.util.function.Consumer;

public interface IChunkService {

    void startOfChunk(PrintWriter output);

    void endOfChunk(PrintWriter output);

    void sendChunk(String string, PrintWriter output);

    ResponseExtractor<Void> getResponseExtractor(Consumer<IPropagatable> consumer, int maxBufferSize);
}
