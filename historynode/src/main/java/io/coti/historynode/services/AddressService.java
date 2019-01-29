package io.coti.historynode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.AddAddressJsonResponse;
import io.coti.historynode.http.GetAddressJsonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@Service
public class AddressService {
    @Autowired
    private ClientService clientService;

    private String ADDRESS_INDEX_NAME = "address";
    private String ADDRESS_OBJECT_NAME = "addressData";

    @PostConstruct
    private void init() {
        try {
            clientService.addIndex(ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public ResponseEntity<IResponse> insertAddressJson(Hash hash, String addressAsJson) throws IOException {
        if (!validateAddress(hash, addressAsJson)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            INVALID_PARAMETERS_MESSAGE,
                            STATUS_ERROR));
        }
        String insertResponse =
                clientService.insertObject(hash, addressAsJson, ADDRESS_INDEX_NAME, ADDRESS_OBJECT_NAME);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddAddressJsonResponse(
                        STATUS_SUCCESS,
                        ADDRESS_CREATED_MESSAGE, insertResponse));
    }

    public ResponseEntity<IResponse> getAddressByHash(Hash hash) throws IOException {
        String addressAsJson = clientService.getObjectByHash(hash, ADDRESS_INDEX_NAME);
        if (addressAsJson == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            ADDRESS_DOESNT_EXIST_MESSAGE,
                            STATUS_ERROR));
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GetAddressJsonResponse(hash, addressAsJson));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            ADDRESS_TRANSACTIONS_SERVER_ERROR ,
                            STATUS_ERROR));
        }
    }

    private boolean validateAddress(Hash hash, String addressAsJsonString) throws IOException {
        // TODO:
        return true;
    }
}
