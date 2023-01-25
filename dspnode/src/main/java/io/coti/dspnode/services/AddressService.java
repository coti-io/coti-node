package io.coti.dspnode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

import static io.coti.dspnode.services.NodeServiceManager.propagationPublisher;

@Slf4j
@Service
@Primary
public class AddressService extends BaseNodeAddressService {

    @Override
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
                NodeType.FinancialServer,
                NodeType.HistoryNode));
    }

    @Override
    protected void continueHandleGeneratedAddress(AddressData addressData) {
        propagationPublisher.propagate(addressData, Collections.singletonList(NodeType.FullNode));
    }
}
