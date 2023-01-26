package io.coti.fullnode.controllers;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.AddressBulkRequest;
import io.coti.basenode.http.AddressRequest;
import io.coti.basenode.http.AddressesExistsResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.AddressStatus;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.fullnode.http.AddAddressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_ADDRESS;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.fullnode.services.NodeServiceManager.addressService;

@Slf4j
@RestController
@RequestMapping("/address")
@Tag(name = "addresses")
public class AddressController {

    @Operation(summary = "Create a new Address", operationId = "addAddress")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Address Created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AddAddressResponse.class))}),
            @ApiResponse(responseCode = "200", description = "Address Exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AddAddressResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid address.",
                    content = @Content)})
    @PutMapping()
    public ResponseEntity<IResponse> addAddress(@Valid @RequestBody AddressRequest addAddressRequest) {

        if (addressService.validateAddress(addAddressRequest.getAddress())) {
            if (addressService.addAddress(addAddressRequest.getAddress())) {
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(new AddAddressResponse(addAddressRequest.getAddress().toHexString(), AddressStatus.CREATED));
            }
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new AddAddressResponse(addAddressRequest.getAddress().toHexString(), AddressStatus.EXISTS));
        } else {
            log.error("Address {} had length error. length: {}", addAddressRequest.getAddress(),
                    addAddressRequest.getAddress().getBytes().length);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(String.format(INVALID_ADDRESS, addAddressRequest.getAddress()), STATUS_ERROR));
        }
    }

    @Operation(summary = "Check Addresses Existence", operationId = "addressExists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AddressesExistsResponse.class))})})
    @PostMapping()
    public ResponseEntity<AddressesExistsResponse> addressExists(@Valid @RequestBody AddressBulkRequest addressRequest) {
        List<Hash> addressHashes = addressRequest.getAddresses();
        AddressesExistsResponse addressResponse = new AddressesExistsResponse();

        addressHashes.forEach(addressHash -> {
            boolean result = addressService.addressExists(addressHash);
            addressResponse.addAddressToResult(addressHash.toHexString(), result);
        });

        return ResponseEntity.status(HttpStatus.OK).body(addressResponse);
    }

    @Operation(summary = "Check Addresses Existence on History Node", operationId = "addressesCheckExistenceAndRequestHistoryNode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AddressesExistsResponse.class))})})
    @PostMapping(value = "/history")
    public ResponseEntity<AddressesExistsResponse> addressesCheckExistenceAndRequestHistoryNode(@Valid @RequestBody AddressBulkRequest addressRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(addressService.addressesCheckExistenceAndRequestHistoryNode(addressRequest));
    }
}