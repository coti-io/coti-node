package io.coti.financialserver.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.financialserver.crypto.RollingReserveCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.GetRollingReserveMerchantAddressRequest;
import io.coti.financialserver.http.GetRollingReserveMerchantAddressResponse;
import io.coti.financialserver.model.RollingReserveAddresses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class RollingReserveService {

    @Value("${financialserver.seed}")
    private String SEED;

    @Autowired
    RollingReserveAddresses rollingReserveAddresses;

    private AtomicLong lastAddressIndex;

    public void init() {
        lastAddressIndex = new AtomicLong();
        rollingReserveAddresses.forEach(c -> lastAddressIndex.getAndIncrement());
    }

    public ResponseEntity geMerchantAddress(GetRollingReserveMerchantAddressRequest request) {

        RollingReserveAddressData rollingReserveAddressData = request.getRollingReserveAddressData();
        RollingReserveCrypto rollingReserveCrypto = new RollingReserveCrypto();
        rollingReserveCrypto.signMessage(rollingReserveAddressData);

        if ( !rollingReserveCrypto.verifySignature(rollingReserveAddressData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        Hash address;

        if( rollingReserveAddresses.getByHash(rollingReserveAddressData.getHash()) == null ) {
            address = CryptoHelper.generateAddress(SEED , lastAddressIndex.intValue());

            rollingReserveAddressData.setRollingReserveAddress(address);
            rollingReserveAddressData.setAddressIndex(lastAddressIndex.intValue());
            rollingReserveAddresses.put(rollingReserveAddressData);

            lastAddressIndex.incrementAndGet();
        }
        else {
            rollingReserveAddressData = rollingReserveAddresses.getByHash(rollingReserveAddressData.getHash());
        }

        return ResponseEntity.status(HttpStatus.OK).body(new GetRollingReserveMerchantAddressResponse(rollingReserveAddressData.getRollingReserveAddress()));
    }
}
