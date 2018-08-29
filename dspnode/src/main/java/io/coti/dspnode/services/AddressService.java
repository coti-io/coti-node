package io.coti.dspnode.services;

import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.data.AddressData;
import io.coti.common.data.NodeType;
import io.coti.common.services.BaseNodeAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class AddressService extends BaseNodeAddressService {
    @Autowired
    private IPropagationPublisher propagationPublisher;

    public String handleNewAddressFromFullNode(AddressData addressData) {
        if (addressExists(addressData.getHash())) {
            log.debug("Address {} exists", addressData.getHash().toHexString());
            return "Address exists";
        }

        addNewAddress(addressData.getHash());
        propagationPublisher.propagate(addressData, Arrays.asList(
                NodeType.FullNode,
                NodeType.TrustScoreNode,
                NodeType.DspNode,
                NodeType.ZeroSpendServer));
        log.debug("Address {} is added", addressData.getHash().toHexString());
        return "OK";
    }

    @Override
    protected void continueHandlePropagatedAddress(AddressData addressData) {
        propagationPublisher.propagate(addressData, Arrays.asList(NodeType.FullNode));
    }
}
