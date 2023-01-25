package io.coti.historynode.services;

import io.coti.basenode.http.AddEntitiesBulkRequest;
import io.coti.basenode.http.AddHistoryEntitiesResponse;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.historynode.crypto.GetTransactionsByAddressRequestCrypto;
import io.coti.historynode.model.AddressTransactionsByAddresses;
import io.coti.historynode.model.AddressTransactionsByDates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@SuppressWarnings({"java:S1104", "java:S1444"})
public class NodeServiceManager extends BaseNodeServiceManager {

    public static AddressTransactionsByAddresses addressTransactionsByAddresses;
    public static AddressTransactionsByDates addressTransactionsByDates;
    public static GetTransactionsByAddressRequestCrypto getTransactionsByAddressRequestCrypto;
    public static StorageConnector<GetHistoryAddressesRequest, GetHistoryAddressesResponse> addressStorageConnector;
    public static StorageConnector<AddEntitiesBulkRequest, AddHistoryEntitiesResponse> entitiesStorageConnector;

    @Autowired
    public AddressTransactionsByAddresses autowiredAddressTransactionsByAddresses;
    @Autowired
    public AddressTransactionsByDates autowiredAddressTransactionsByDates;
    @Autowired
    public GetTransactionsByAddressRequestCrypto autowiredGetTransactionsByAddressRequestCrypto;
    @Autowired
    public StorageConnector<GetHistoryAddressesRequest, GetHistoryAddressesResponse> autowiredAddressStorageConnector;
    @Autowired
    public StorageConnector<AddEntitiesBulkRequest, AddHistoryEntitiesResponse> autowiredEntitiesStorageConnector;

}
