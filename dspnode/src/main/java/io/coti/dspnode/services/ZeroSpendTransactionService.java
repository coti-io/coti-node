package io.coti.dspnode.services;


import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.ZeroSpendTransactionRequest;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ZeroSpendTransactionService {

    @Autowired
    private INetworkService networkService;
    @Autowired
    private ISender sender;

    public String handleReceivedZeroSpendTransactionRequest(ZeroSpendTransactionRequest zeroSpendTransactionRequest) {
        log.info("New ZeroSpend transaction request received");
        String zerospendReceivingAddress = networkService.getSingleNodeData(NodeType.ZeroSpendServer).getReceivingFullAddress();
        sender.send(zeroSpendTransactionRequest, zerospendReceivingAddress);
        return "Ok";
    }
}
