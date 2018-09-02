package io.coti.dspnode.services;


import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.ZeroSpendTransactionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ZeroSpendTransactionService {

    @Value("${zerospend.receiving.address}")
    private String zerospendReceivingAddress;

    @Autowired
    private ISender sender;

    public String handleReceivedZeroSpendTransactionRequest(ZeroSpendTransactionRequest zeroSpendTransactionRequest) {
        log.info("New ZeroSpend transaction request received");
        sender.send(zeroSpendTransactionRequest, zerospendReceivingAddress);
        return "Ok";
    }
}
