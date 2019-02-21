package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.GetEntitiesBulkRequest;
import io.coti.historynode.http.storageConnector.interaces.IStorageConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
public class AddressService extends EntityService implements io.coti.historynode.services.interfaces.IAddressService {
    @Autowired
    protected IStorageConnector storageConnector;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        endpoint = "/addresses";
    }

    @Override
    public ResponseEntity<IResponse> getAddresses(List<Hash> addresses) {
        GetEntitiesBulkRequest getEntitiesBulkRequest = new GetEntitiesBulkRequest();
        getEntitiesBulkRequest.setHashes(addresses);
        return storageConnector.getForObject(storageServerAddress + endpoint, ResponseEntity.class, getEntitiesBulkRequest);
    }
}
