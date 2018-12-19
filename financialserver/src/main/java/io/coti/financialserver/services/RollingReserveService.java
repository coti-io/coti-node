package io.coti.financialserver.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.Response;
import io.coti.financialserver.crypto.RollingReserveCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.GetRollingReserveMerchantAddressRequest;
import io.coti.financialserver.http.GetRollingReserveMerchantAddressResponse;
import io.coti.financialserver.http.GetRollingReserveReleaseDatesResponse;
import io.coti.financialserver.model.RollingReserves;
import io.coti.financialserver.model.RollingReserveReleaseDates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class RollingReserveService {

    @Value("${financialserver.seed}")
    private String SEED;

    @Autowired
    private DisputeService disputeService;

    @Autowired
    RollingReserves rollingReserves;

    @Autowired
    RollingReserveReleaseDates rollingReserveReleaseDates;

    private AtomicInteger lastAddressIndex;

    public void init() {
        lastAddressIndex = new AtomicInteger(0);
        rollingReserves.forEach(c -> lastAddressIndex.getAndIncrement());
    }

    public ResponseEntity geMerchantAddress(GetRollingReserveMerchantAddressRequest request) {

        RollingReserveData rollingReserveData = request.getRollingReserveData();
        RollingReserveCrypto rollingReserveCrypto = new RollingReserveCrypto();
        rollingReserveCrypto.signMessage(rollingReserveData);

        if ( !rollingReserveCrypto.verifySignature(rollingReserveData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        if( rollingReserves.getByHash(rollingReserveData.getHash()) == null ) {
            createRollingReserveDataForMerchant(rollingReserveData.getHash());
        }

        rollingReserveData = rollingReserves.getByHash(rollingReserveData.getHash());
        return ResponseEntity.status(HttpStatus.OK).body(new GetRollingReserveMerchantAddressResponse(rollingReserveData.getRollingReserveAddress()));
    }

    public ResponseEntity getRollingReserveRelease(GetRollingReserveMerchantAddressRequest request) {
        RollingReserveReleaseDateData rollingReserveReleaseDateData;
        RollingReserveReleaseStatus rollingReserveReleaseStatus;

        RollingReserveData rollingReserveData = request.getRollingReserveData();
        RollingReserveCrypto rollingReserveCrypto = new RollingReserveCrypto();
        rollingReserveCrypto.signMessage(rollingReserveData);

        if ( !rollingReserveCrypto.verifySignature(rollingReserveData) ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        rollingReserveData = rollingReserves.getByHash(rollingReserveData.getHash());

        Map<String, RollingReserveReleaseStatus> rollingReserveReleases = new HashMap<>();
        for(Date releaseData : rollingReserveData.getReleaseDates()) {

            Hash dateHash = new Hash(releaseData.getTime());
            rollingReserveReleaseDateData = rollingReserveReleaseDates.getByHash(dateHash);
            rollingReserveReleaseStatus = rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().get(rollingReserveData.getHash());

            rollingReserveReleases.put(releaseData.toString(), rollingReserveReleaseStatus);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new GetRollingReserveReleaseDatesResponse(rollingReserveData, rollingReserveReleases));
    }

    public void setRollingReserveReleaseDate(TransactionData transactionData) {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Hash dateHash = new Hash(date.getTime());
        Hash merchantHash = disputeService.getMerchantHash(transactionData.getReceiverBaseTransactionHash());
        merchantHash = new Hash("5fa4221a7305c89e04a4eb9b985820d4121bacad331d5ef2d3097bad2e80527ac594b87784ec43a8fd26f7561c9a4c3ca9ca40d6bc26287bb9c48d7c649e64e0");
        RollingReserveReleaseDateData rollingReserveReleaseDateData = rollingReserveReleaseDates.getByHash(dateHash);

        if(rollingReserveReleaseDateData == null) {
            rollingReserveReleaseDateData = new RollingReserveReleaseDateData();
            rollingReserveReleaseDateData.setHash(dateHash);
            rollingReserveReleaseDateData.setDate(date);
        }

        RollingReserveReleaseStatus rollingReserveReleaseStatus = rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().get(merchantHash);

        if(rollingReserveReleaseStatus == null) {
            rollingReserveReleaseStatus = new RollingReserveReleaseStatus();
            rollingReserveReleaseStatus.setInitialAmount(transactionData.getAmount()); // TODO should be RR amount
            rollingReserveReleaseStatus.getPaymentTransactions().add(transactionData.getHash());
        }
        else {
            rollingReserveReleaseStatus.addToInitialAmount(transactionData.getAmount()); // TODO should be RR amount
            rollingReserveReleaseStatus.getPaymentTransactions().add(transactionData.getHash());
        }

        rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().put(merchantHash, rollingReserveReleaseStatus);

        if( rollingReserves.getByHash(merchantHash) == null ) {
            createRollingReserveDataForMerchant(merchantHash);
        }

        RollingReserveData rollingReserveData = rollingReserves.getByHash(merchantHash);

        rollingReserveReleaseDates.put(rollingReserveReleaseDateData);
        if( !rollingReserveData.getReleaseDates().contains(date) ) {
            rollingReserveData.getReleaseDates().add(date);
        }

        rollingReserves.put(rollingReserveData);
    }

    private void createRollingReserveDataForMerchant(Hash merchantHash) {

        if(lastAddressIndex == null) {
            init();
        }

        Hash address = CryptoHelper.generateAddress(SEED , lastAddressIndex.intValue());

        RollingReserveData rollingReserveData = new RollingReserveData();
        rollingReserveData.setMerchantHash(merchantHash);
        rollingReserveData.setRollingReserveAddress(address);
        rollingReserveData.setAddressIndex(lastAddressIndex.intValue());
        rollingReserves.put(rollingReserveData);

        lastAddressIndex.incrementAndGet();
    }
}
