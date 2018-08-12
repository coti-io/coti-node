package io.coti.common.services;

import io.coti.common.communication.ZeroMQUtils;
import io.coti.common.communication.interfaces.ISender;
import io.coti.common.communication.interfaces.ISerializer;
import io.coti.common.data.AddressData;
import io.coti.common.data.DspVote;
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
    @Value("${zerospend.receiving.address}")
    private String zeroSpendServerAddress;
    private ZMQ.Context zeroMQContext;
    private ZMQ.Socket transactionSender;
    private ZMQ.Socket dspVoteSender;

    @Autowired
    private ISerializer serializer;

    @PostConstruct
    private void init() {
        zeroMQContext = ZMQ.context(1);
        transactionSender = zeroMQContext.socket(ZMQ.DEALER);
        ZeroMQUtils.bindToRandomPort(transactionSender);
        for (String receivingServerAddress :
                receivingServerAddresses
        ) {
            transactionSender.connect(receivingServerAddress);
        }
        if (zeroSpendServerAddress != null) {
            dspVoteSender = zeroMQContext.socket(ZMQ.DEALER);
            ZeroMQUtils.bindToRandomPort(dspVoteSender);
            dspVoteSender.connect(zeroSpendServerAddress);
        }
    }

    @Override
    public void sendAddress(AddressData addressData) {
        byte[] message = serializer.serialize(addressData);
        synchronized (zeroMQContext) {
            try {
                transactionSender.sendMore(AddressData.class.getName());
                transactionSender.send(message);
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
                transactionSender.sendMore(TransactionData.class.getName());
                transactionSender.send(message);
            } catch (ZMQException exception) {
                return;
            }
        }
    }

    public void sendDspVote(DspVote dspVote) {
        log.info("Sending DSP Vote: {}={}", dspVote.getTransactionHash(), dspVote.isValidTransaction());
        byte[] message = serializer.serialize(dspVote);
        synchronized (zeroMQContext) {
            try {
                dspVoteSender.sendMore(DspVote.class.getName());
                dspVoteSender.send(message);
            } catch (ZMQException exception) {
                return;
            }
        }
    }
}