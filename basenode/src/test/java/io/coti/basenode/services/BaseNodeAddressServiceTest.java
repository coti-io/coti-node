package io.coti.basenode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.RequestedAddressHashData;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.interfaces.IAddressService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.function.BiConsumer;

import static io.coti.basenode.services.BaseNodeAddressService.TRUSTED_RESULT_MAX_DURATION_IN_MILLIS;
import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static testUtils.BaseNodeTestUtils.generateRandomAddressData;

@ContextConfiguration(classes = {BaseNodeAddressService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeAddressServiceTest {

    @Autowired
    private IAddressService addressService;
    @MockBean
    private Addresses addressesLocal;
    @MockBean
    private IValidationService validationServiceLocal;
    @MockBean
    private FileService fileServiceLocal;

    @BeforeEach
    public void init() {
        addresses = addressesLocal;
        validationService = validationServiceLocal;
        fileService = fileServiceLocal;
        addressService.init();
    }

    @Test
    void addNewAddress_existingAddress_notAdded() {
        AddressData addressData = generateRandomAddressData();
        when(addresses.getByHash(any(Hash.class))).thenReturn(addressData);

        boolean addedNewAddress = addressService.addNewAddress(addressData);
        verify(addresses, never()).put(addressData);
        assertFalse(addedNewAddress);
    }

    @Test
    void addNewAddress_nonExistingAddress_added() {
        AddressData addressData = generateRandomAddressData();
        when(addresses.getByHash(any(Hash.class))).thenReturn(null);

        boolean addedNewAddress = addressService.addNewAddress(addressData);
        verify(addresses, times(1)).put(addressData);
        assertTrue(addedNewAddress);
    }

    @Test
    void handlePropagatedAddress_addressExists_notAdded() {
        AddressData addressData = generateRandomAddressData();
        when(addresses.getByHash(any(Hash.class))).thenReturn(addressData);

        addressService.handlePropagatedAddress(addressData);
        verify(addresses, never()).put(addressData);
    }

    @Test
    void handlePropagatedAddress_addressInvalid_notAdded() {
        AddressData addressData = generateRandomAddressData();
        when(addresses.getByHash(any(Hash.class))).thenReturn(null);
        when(validationService.validateAddress(any(Hash.class))).thenReturn(false);

        addressService.handlePropagatedAddress(addressData);
        verify(addresses, never()).put(addressData);
    }

    @Test
    void handlePropagatedAddress_addressNewValid_added() {
        AddressData addressData = generateRandomAddressData();
        when(addresses.getByHash(any(Hash.class))).thenReturn(null);
        when(validationService.validateAddress(any(Hash.class))).thenReturn(true);

        addressService.handlePropagatedAddress(addressData);
        verify(addresses, times(1)).put(addressData);
    }

    @Test
    void handlePropagatedAddress_throwException_notAdded() {
        AddressData addressData = generateRandomAddressData();
        when(addresses.getByHash(any(Hash.class))).thenReturn(null);
        when(validationService.validateAddress(any(Hash.class))).thenThrow(NullPointerException.class);

        addressService.handlePropagatedAddress(addressData);
        verify(addresses, never()).put(addressData);
    }

    @Test
    void getAddressBatch_noAddressFound_noneReturned() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        addressService.getAddressBatch(response);
        verify(addresses, times(1)).forEachWithLastIteration(any(BiConsumer.class));
    }

    @Test
    void validateRequestedAddressHashExistsAndRelevant_emptyRequest_validationFails() {
        boolean addressValidated = addressService.validateRequestedAddressHashExistsAndRelevant(null);
        assertFalse(addressValidated);
    }

    @Test
    void validateRequestedAddressHashExistsAndRelevant_requestTimedOut_validationFails() {
        AddressData addressData = generateRandomAddressData();
        RequestedAddressHashData requestedAddressHashData = new RequestedAddressHashData(addressData.getHash());
        requestedAddressHashData.setLastUpdateTime(requestedAddressHashData.getLastUpdateTime().minusMillis(TRUSTED_RESULT_MAX_DURATION_IN_MILLIS - 1000));
        boolean addressValidated = addressService.validateRequestedAddressHashExistsAndRelevant(requestedAddressHashData);
        assertTrue(addressValidated);
    }

    @Test
    void validateRequestedAddressHashExistsAndRelevant_requestNotTimedOut_validationPasses() {
        AddressData addressData = generateRandomAddressData();
        RequestedAddressHashData requestedAddressHashData = new RequestedAddressHashData(addressData.getHash());
        requestedAddressHashData.setLastUpdateTime(requestedAddressHashData.getLastUpdateTime().minusMillis(TRUSTED_RESULT_MAX_DURATION_IN_MILLIS + 1000));
        boolean addressValidated = addressService.validateRequestedAddressHashExistsAndRelevant(requestedAddressHashData);
        assertFalse(addressValidated);
    }

}
