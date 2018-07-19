package io.coti.common.communication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import java.util.function.Consumer;

@Slf4j
@Service
public class ZeroMQDirectConnectionServer {
    ZMQ.Context zeroMQContext;
    ZMQ.Socket server;

    public void init(String serverAddress){
        System.out.println("Server starting...");
        zeroMQContext = ZMQ.context(1);
        server = zeroMQContext.socket(ZMQ.REP);
        server.bind(serverAddress);
        System.out.println("Zero MQ Server Connected!");
    }

    public void runReceiveLoop(Consumer<byte[]> messageHandler) {
        while(true){
            byte[] message = server.recv();
            log.info("Received a new message...");
            messageHandler.accept(message);
            server.send("Received a message...");
        }
    }
}
