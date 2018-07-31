package io.coti.common.services;

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
    private List<String> receivingDspServerAddresses;
    @Value("${receiving.zero.spend.address}")
    private String receivingZeroSpendAddress;
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket sender;
    private ZMQ.Socket zeroSpendSender;

    @Autowired
    private ISerializer serializer;

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        sender = zeroMQContext.socket(ZMQ.DEALER);
        ZeroMQUtils.bindToRandomPort(sender);
        for (String receivingServerAddress :
                receivingDspServerAddresses
                ) {
            sender.connect(receivingServerAddress);
        }
        zeroSpendSender = zeroMQContext.socket(ZMQ.DEALER);
        ZeroMQUtils.bindToRandomPort(zeroSpendSender);
        zeroSpendSender.connect(receivingZeroSpendAddress);
    }

    @Override
    public void sendAddress(AddressData addressData) {
        byte[] message = serializer.serialize(addressData);
        synchronized (sender) {
            try {
                sender.sendMore(AddressData.class.getName());
                sender.send(message);
            } catch (ZMQException exception) {
                return;
            }
        }
    }

    @Override
    public void sendTransactionToDsps(TransactionData transactionData) {
        byte[] message = serializer.serialize(transactionData);
        synchronized (sender) {
            try {
                sender.sendMore(TransactionData.class.getName());
                sender.send(message);
            } catch (ZMQException exception) {
                return;
            }
        }
    }

    @Override
    public void sendTransactionToZeroSpend(TransactionData transactionData) {
        byte[] message = serializer.serialize(transactionData);
        synchronized (zeroSpendSender) {
            try {
                zeroSpendSender.sendMore(TransactionData.class.getName());
                zeroSpendSender.send(message);
            } catch (ZMQException exception) {
                return;
            }
        }
    }

    public void sendDspVote(DspVote dspVote){
        log.info("Sending DSP Vote: {}={}", dspVote.transactionHash, dspVote.isValidTransaction);
        byte[] message = serializer.serialize(dspVote);
        synchronized (sender) {
            try {
                sender.sendMore(DspVote.class.getName());
                sender.send(message);
            } catch (ZMQException exception) {
                return;
            }
        }
    }
}