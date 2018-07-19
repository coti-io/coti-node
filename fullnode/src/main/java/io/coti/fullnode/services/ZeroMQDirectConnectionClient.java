package io.coti.fullnode.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;

@Slf4j
@Service
public class ZeroMQDirectConnectionClient {
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket client;
    @Value("${dsp.receiver.address}")
    private String dspReceiverAddress;

    public void init(String senderSocketAddress, String dspReceiverAddress) {
        zeroMQContext = ZMQ.context(1);
        client = zeroMQContext.socket(ZMQ.REQ);
        client.bind(senderSocketAddress);
        client.connect(dspReceiverAddress);
    }

    public void send(byte[] message) {
        try {
            client.send(message);
            String response = client.recvStr();
            log.info(response);
        }
        catch (IllegalStateException e){
            log.error("Did not receive approval for last message from DSP...");
            e.printStackTrace();
        }
    }
}
