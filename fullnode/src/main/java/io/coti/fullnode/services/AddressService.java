package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeAddressService;
import io.coti.basenode.services.interfaces.INetworkDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class AddressService extends BaseNodeAddressService {

    @Autowired
    private WebSocketSender webSocketSender;

    @Autowired
    private ISender sender;
    @Autowired
    private INetworkDetailsService networkDetailsService;

    public boolean addAddress(Hash addressHash) {
        List<String> receivingServerAddresses = new LinkedList<>();
        receivingServerAddresses.add(networkDetailsService.getShuffledNetworkNodeDataListFromMapValues(networkDetailsService.getNetworkDetails()
                .getDspNetworkNodesMap()).get(0).getReceivingFullAddress());
        if (!super.addNewAddress(addressHash)) {
            return false;
        }
        AddressData newAddressData = new AddressData(addressHash);
        receivingServerAddresses.forEach(address -> sender.send(newAddressData, address));
        continueHandleGeneratedAddress(newAddressData);
        return true;
    }


    @Override
    protected void continueHandleGeneratedAddress(AddressData addressData) {
        webSocketSender.notifyGeneratedAddress(addressData.getHash());
    }

}
