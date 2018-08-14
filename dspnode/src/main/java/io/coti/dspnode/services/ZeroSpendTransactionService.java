package io.coti.dspnode.services;

import io.coti.common.communication.interfaces.ISender;
import io.coti.common.data.AddressData;
import io.coti.common.data.ZeroSpendTransactionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ZeroSpendTransactionService {

    @Value("#{'${zerospend.receiving.address}'.split(',')}")
    private List<String> zerospendReceivingAddress;
    
    @Autowired
    private ISender sender;

    public  String handleReceivedZeroSpendTransactionRequest(ZeroSpendTransactionRequest zeroSpendTransactionRequest) {
        log.info("A ZS trx request was received in DSP node and about to be send to zs node");
        try {
            zerospendReceivingAddress.forEach(address -> sender.send(zeroSpendTransactionRequest, address));
        }
        catch (Exception e){
            log.error("Error while trying to send to zs ", e);
            return "Bad!";
        }
        return "Good!";
    }
}
