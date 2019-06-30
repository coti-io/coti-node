package io.coti.historynode.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetEntitiesBulkRequest;
import io.coti.basenode.http.GetEntityRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.storageConnector.interaces.IStorageConnector;
import io.coti.historynode.services.interfaces.IHistoryAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
public class HistoryAddressService extends EntityService implements IHistoryAddressService {
    @Autowired
    protected IStorageConnector storageConnector;

    @PostConstruct
    public void init() {
        mapper = new ObjectMapper();
        endpoint = "/transactionsAddresses";
    }

    @Override
    public ResponseEntity<IResponse> getAddress(Hash address) {
        GetEntityRequest getEntityRequest = new GetEntityRequest(address);
        return storageConnector.getForObject(storageServerAddress + endpoint, ResponseEntity.class, getEntityRequest);
    }

    @Override
    public ResponseEntity<IResponse> getAddressFromHistory(Hash address) {
        return getAddress(address);
    }

}
