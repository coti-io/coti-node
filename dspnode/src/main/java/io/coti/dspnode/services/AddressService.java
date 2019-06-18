package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class AddressService extends BaseNodeAddressService {
    @Autowired
    private IPropagationPublisher propagationPublisher;

    public void handleNewAddressFromFullNode(AddressData addressData) {
        if (addressExists(addressData.getHash())) {
            log.debug("Address {} exists", addressData.getHash());
            return;
        }
        if (!validateAddress(addressData.getHash())) {
            log.error("Invalid address {}", addressData.getHash());
            return;
        }

        addNewAddress(addressData);
        propagationPublisher.propagate(addressData, Arrays.asList(
                NodeType.FullNode,
                NodeType.TrustScoreNode,
                NodeType.DspNode,
                NodeType.ZeroSpendServer,
                NodeType.FinancialServer));
    }

    @Override
    protected void continueHandleGeneratedAddress(AddressData addressData) {
        propagationPublisher.propagate(addressData, Arrays.asList(NodeType.FullNode));
    }
}
