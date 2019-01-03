package io.coti.financialserver.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.Response;
import io.coti.basenode.model.Transactions;
import io.coti.financialserver.crypto.RecourseClaimCrypto;
import io.coti.financialserver.crypto.RollingReserveCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.GetRollingReserveMerchantDataRequest;
import io.coti.financialserver.http.GetRollingReserveReleaseDatesResponse;
import io.coti.financialserver.http.RecourseClaimRequest;
import io.coti.financialserver.model.Disputes;
import io.coti.financialserver.model.RecourseClaims;
import io.coti.financialserver.model.RollingReserveReleaseDates;
import io.coti.financialserver.model.RollingReserves;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class RollingReserveService {

    private static final int COTI_POOL_ADDRESS_INDEX = 0;
    private static final int ROLLING_RESERVE_DEFAULT_DAYS_TO_HOLD = 10;

    @Value("${financialserver.seed}")
    private String SEED;

    @Autowired
    private DisputeService disputeService;
    @Autowired
    RollingReserves rollingReserves;
    @Autowired
    RollingReserveReleaseDates rollingReserveReleaseDates;
    @Autowired
    TransactionCreationService transactionCreationService;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    RecourseClaims recourseClaims;
    @Autowired
    private Transactions transactions;
    @Autowired
    private Disputes disputes;

    private AtomicInteger lastAddressIndex;

    public void init() {
        lastAddressIndex = new AtomicInteger(COTI_POOL_ADDRESS_INDEX + 1);
        rollingReserves.forEach(c -> lastAddressIndex.getAndIncrement());
    }

    public Hash getCotiPoolAddress() {
        return CryptoHelper.generateAddress(SEED, COTI_POOL_ADDRESS_INDEX);
    }

    public ResponseEntity getRollingReserveData(GetRollingReserveMerchantDataRequest request) {

        RollingReserveReleaseDateData rollingReserveReleaseDateData;
        RollingReserveReleaseStatus rollingReserveReleaseStatus;

        RollingReserveData rollingReserveData = request.getRollingReserveData();
        RollingReserveCrypto rollingReserveCrypto = new RollingReserveCrypto();
        rollingReserveCrypto.signMessage(rollingReserveData);

        if (!rollingReserveCrypto.verifySignature(rollingReserveData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        if (rollingReserves.getByHash(rollingReserveData.getHash()) == null) {
            createRollingReserveDataForMerchant(rollingReserveData.getHash());
            rollingReserveData = rollingReserves.getByHash(rollingReserveData.getHash());
            propagationPublisher.propagate(new RollingReserveAddressPropagatable(rollingReserveData.getHash(), rollingReserveData.getRollingReserveAddress()),
                    Arrays.asList(NodeType.TrustScoreNode));
        } else {
            rollingReserveData = rollingReserves.getByHash(rollingReserveData.getHash());
        }

        Map<String, RollingReserveReleaseStatus> rollingReserveReleases = new HashMap<>();
        for (Date releaseData : rollingReserveData.getReleaseDates()) {

            Hash dateHash = new Hash(releaseData.getTime());
            rollingReserveReleaseDateData = rollingReserveReleaseDates.getByHash(dateHash);
            rollingReserveReleaseStatus = rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().get(rollingReserveData.getHash());

            rollingReserveReleases.put(releaseData.toString(), rollingReserveReleaseStatus);
        }

        RecourseClaimData recourseClaimData = recourseClaims.getByHash(rollingReserveData.getMerchantHash());

        return ResponseEntity.status(HttpStatus.OK).body(new GetRollingReserveReleaseDatesResponse(rollingReserveData, rollingReserveReleases, recourseClaimData));
    }

    public ResponseEntity recourseClaim(RecourseClaimRequest request) {

        RecourseClaimData recourseClaimData = request.getRecourseClaimData();
        RecourseClaimCrypto recourseClaimCrypto = new RecourseClaimCrypto();
        recourseClaimCrypto.signMessage(recourseClaimData);

        if (!recourseClaimCrypto.verifySignature(recourseClaimData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        TransactionData transactionData = transactions.getByHash(recourseClaimData.getTransactionHashes().iterator().next());

        if (transactionData == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_TRANSACTION_NOT_FOUND, STATUS_ERROR));
        }

        if (!transactionData.getReceiverBaseTransactionAddressHash().equals(getCotiPoolAddress())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(NOT_COTI_POOL, STATUS_ERROR));
        }

        DisputeData disputeData = disputes.getByHash(recourseClaimData.getDisputeHashes().iterator().next());

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_NOT_FOUND, STATUS_ERROR));
        }
        if (!disputeData.getMerchantHash().equals(recourseClaimData.getMerchantHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_UNAUTHORIZED, STATUS_ERROR));
        }

        recourseClaimData = recourseClaims.getByHash(recourseClaimData.getMerchantHash());

        if (recourseClaimData.getTransactionHashes().contains(transactionData)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ALREADY_GOT_THIS_RECOURSE_CLAIM, STATUS_ERROR));
        }

        if (transactionData.getAmount().compareTo(recourseClaimData.getAmountToPay()) < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(NOT_ENOUGH_MONEY_IN_TRANSACTION, STATUS_ERROR));
        }

        recourseClaimData.setAmountToPay(new BigDecimal(0));
        recourseClaimData.getTransactionHashes().add(transactionData.getHash());
        recourseClaims.put(recourseClaimData);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

    public void setRollingReserveReleaseDate(TransactionData transactionData, Hash merchantHash) {

        // TODO: Get number of days from transaction data
        Date date = getDateNumberOfDaysAfterToday(ROLLING_RESERVE_DEFAULT_DAYS_TO_HOLD);

        Hash dateHash = new Hash(date.getTime());
        RollingReserveReleaseDateData rollingReserveReleaseDateData = rollingReserveReleaseDates.getByHash(dateHash);

        if (rollingReserveReleaseDateData == null) {
            rollingReserveReleaseDateData = new RollingReserveReleaseDateData(date);
        }

        RollingReserveReleaseStatus rollingReserveReleaseStatus = rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().get(merchantHash);

        if (rollingReserveReleaseStatus == null) {
            rollingReserveReleaseStatus = new RollingReserveReleaseStatus(transactionData.getRollingReserveAmount(), transactionData.getHash());
        } else {
            rollingReserveReleaseStatus.addToInitialAmount(transactionData.getRollingReserveAmount());
            rollingReserveReleaseStatus.getPaymentTransactions().add(transactionData.getHash());
        }

        rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().put(merchantHash, rollingReserveReleaseStatus);

        if (rollingReserves.getByHash(merchantHash) == null) {
            createRollingReserveDataForMerchant(merchantHash);
        }

        RollingReserveData rollingReserveData = rollingReserves.getByHash(merchantHash);

        rollingReserveReleaseDates.put(rollingReserveReleaseDateData);
        if (!rollingReserveData.getReleaseDates().contains(date)) {
            rollingReserveData.getReleaseDates().add(date);
        }

        rollingReserves.put(rollingReserveData);
    }

    public void chargebackConsumer(DisputeData disputeData, Hash consumerAddress, BigDecimal amount) {

        Hash dateHash;
        RollingReserveReleaseDateData rollingReserveReleaseDateData;
        Hash merchantHash = disputeData.getMerchantHash();

        if (rollingReserves.getByHash(merchantHash) == null) {
            createRollingReserveDataForMerchant(merchantHash);
        }
        RollingReserveData rollingReserveData = rollingReserves.getByHash(merchantHash);
        List<Date> releaseDates = rollingReserveData.getReleaseDates();

        BigDecimal remainingChargebackAmount = amount;
        for (Date releaseDate : releaseDates) {
            dateHash = new Hash(releaseDate.getTime());
            rollingReserveReleaseDateData = rollingReserveReleaseDates.getByHash(dateHash);
            RollingReserveReleaseStatus rollingReserveReleaseStatus = rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().get(merchantHash);

            if (rollingReserveReleaseStatus.getRemainingAmount().compareTo(remainingChargebackAmount) < 0) {

                remainingChargebackAmount = remainingChargebackAmount.subtract(rollingReserveReleaseStatus.getRemainingAmount());
                rollingReserveReleaseStatus.setReturnedAmount(rollingReserveReleaseStatus.getInitialAmount());
                addConsumerToRollingReserveReceiver(rollingReserveReleaseStatus);

                rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().put(merchantHash, rollingReserveReleaseStatus);
                rollingReserveReleaseDates.put(rollingReserveReleaseDateData);
            } else {

                rollingReserveReleaseStatus.setReturnedAmount(rollingReserveReleaseStatus.getReturnedAmount().add(remainingChargebackAmount));
                if (rollingReserveReleaseStatus.getInitialAmount().equals(rollingReserveReleaseStatus.getReturnedAmount())) {
                    addConsumerToRollingReserveReceiver(rollingReserveReleaseStatus);
                    rollingReserveData.getReleaseDates().remove(releaseDate);
                }

                remainingChargebackAmount = new BigDecimal(0);

                rollingReserveReleaseDates.put(rollingReserveReleaseDateData);
                break;
            }
        }

        transactionCreationService.createNewChargebackTransaction(amount, rollingReserveData.getRollingReserveAddress(), consumerAddress, remainingChargebackAmount);

        if (!remainingChargebackAmount.equals(new BigDecimal(0))) {
            RecourseClaimData recourseClaimData = recourseClaims.getByHash(merchantHash);
            if (recourseClaimData == null) {
                recourseClaimData = new RecourseClaimData();
                recourseClaimData.setHash(merchantHash);
            }

            recourseClaimData.getDisputeHashes().add(disputeData.getHash());
            recourseClaimData.setAmountToPay(recourseClaimData.getAmountToPay().add(remainingChargebackAmount));

            propagationPublisher.propagate(recourseClaimData, Arrays.asList(NodeType.TrustScoreNode));
            recourseClaims.put(recourseClaimData);
        }

        rollingReserves.put(rollingReserveData);
    }

    private void createRollingReserveDataForMerchant(Hash merchantHash) {

        if (lastAddressIndex == null) {
            init();
        }

        Hash address = CryptoHelper.generateAddress(SEED, lastAddressIndex.intValue());

        RollingReserveData rollingReserveData = new RollingReserveData();
        rollingReserveData.setMerchantHash(merchantHash);
        rollingReserveData.setRollingReserveAddress(address);
        rollingReserveData.setAddressIndex(lastAddressIndex.intValue());
        rollingReserves.put(rollingReserveData);

        lastAddressIndex.incrementAndGet();
    }

    private void addConsumerToRollingReserveReceiver(RollingReserveReleaseStatus rollingReserveReleaseStatus) {

        if (rollingReserveReleaseStatus.getRollingReserveReceiver() == RollingReserveReceiver.Merchant) {
            rollingReserveReleaseStatus.setRollingReserveReceiver(RollingReserveReceiver.MerchantAndConsumer);
        } else {
            rollingReserveReleaseStatus.setRollingReserveReceiver(RollingReserveReceiver.Consumer);
        }
    }

    private Date getDateNumberOfDaysAfterToday(int numberOfDays) {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try {
            date = formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateUtils.addDays(date, numberOfDays);

        return date;
    }
}
