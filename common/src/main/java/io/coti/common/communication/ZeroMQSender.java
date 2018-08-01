package io.coti.common.communication;

import io.coti.common.communication.DspVote;
import io.coti.common.communication.ZeroMQUtils;
import io.coti.common.communication.interfaces.ISender;
import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.data.AddressData;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
public class ZeroMQSender implements ISender {
    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket sender;

    @Autowired
    private ISerializer serializer;

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        sender = zeroMQContext.socket(ZMQ.DEALER);
        ZeroMQUtils.bindToRandomPort(sender);
        for (String receivingServerAddress :
                receivingServerAddresses
                ) {
            sender.connect(receivingServerAddress);
        }
    }

    @Override
    public void sendAddress(AddressData addressData) {
        byte[] message = serializer.serialize(addressData);
        synchronized (zeroMQContext) {
            try {
                sender.sendMore(AddressData.class.getName());
                sender.send(message);
            } catch (ZMQException exception) {
                return;
            }
        }
    }

    @Override
    public void sendTransaction(TransactionData transactionData) {
        byte[] message = serializer.serialize(transactionData);
        synchronized (zeroMQContext) {
            try {
                sender.sendMore(TransactionData.class.getName());
                sender.send(message);
            } catch (ZMQException exception) {
                return;
            }
        }
    }

    public void sendDspVote(DspVote dspVote){
        log.info("Sending DSP Vote: {}={}", dspVote.getTransactionHash(), dspVote.getIsValidTransaction());
        byte[] message = serializer.serialize(dspVote);
        synchronized (zeroMQContext) {
            try {
                sender.sendMore(DspVote.class.getName());
                sender.send(message);
            } catch (ZMQException exception) {
                return;
            }
        }
    }
}