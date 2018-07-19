package io.coti.common.communication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class MultiClientZeroMQPublisher {
    @Value("${dsp.propagation.address}")
    private String serverAddress;

    private ZMQ.Socket publisher;
    private ZMQ.Context zeroMQContext;
    private List<String> connectedClients = new LinkedList<>();

    public void init(String serverAddress) {
        zeroMQContext = ZMQ.context(1);
        publisher = zeroMQContext.socket(ZMQ.PUB);
        publisher.bind(serverAddress);
        Thread newClientsListener = new Thread(() -> {
            ZMQ.Socket newClientsReceiver = zeroMQContext.socket(ZMQ.REP);
            newClientsReceiver.bind("tcp://localhost:8006");
            while (!Thread.interrupted()) {
                String clientId = newClientsReceiver.recvStr();
                connectedClients.add(clientId);
                log.info("New client connected: {}", clientId);
                newClientsReceiver.send("Welcome, " + clientId);
            }
        });
        newClientsListener.start();

        log.info("Server Connected!");
    }

    public List<String> getConnectedClients() {
        // Send a message to all clients for their response
        publisher.sendMore("ClientsInfo");
        publisher.send("Information");
        return connectedClients;
    }

    public void sendToAll(String channel, byte[] message) {
        publisher.sendMore(channel);
        publisher.send(message);
    }
}
