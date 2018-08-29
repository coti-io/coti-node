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
    protected void continueHandleCreatedAddress(AddressData addressData) {
        propagationPublisher.propagate(addressData, Arrays.asList(NodeType.FullNode));
    }
}
