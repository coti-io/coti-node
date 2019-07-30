package io.coti.financialserver.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.GetMerchantRollingReserveAddressCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.MerchantRollingReserveAddressData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetMerchantRollingReserveAddressRequest;
import io.coti.basenode.http.GetMerchantRollingReserveAddressResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.SerializableResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.financialserver.crypto.MerchantRollingReserveCrypto;
import io.coti.financialserver.crypto.RecourseClaimCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.GetMerchantRollingReserveDataRequest;
import io.coti.financialserver.http.GetRollingReserveReleaseDatesResponse;
import io.coti.financialserver.http.RecourseClaimRequest;
import io.coti.financialserver.model.Disputes;
import io.coti.financialserver.model.MerchantRollingReserves;
import io.coti.financialserver.model.RecourseClaims;
import io.coti.financialserver.model.RollingReserveReleaseDates;
import io.coti.financialserver.utils.DatesHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class RollingReserveService {

    private static final int COTI_ROLLING_RESERVE_ADDRESS_INDEX = Math.toIntExact(ReservedAddress.ROLLING_RESERVE_POOL.getIndex());
    private static final int ROLLING_RESERVE_DEFAULT_DAYS_TO_HOLD = 10;
    @Autowired
    private MerchantRollingReserves merchantRollingReserves;
    @Autowired
    private RollingReserveReleaseDates rollingReserveReleaseDates;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private RecourseClaims recourseClaims;
    @Value("${financialserver.seed}")
    private String seed;
    @Autowired
    private MerchantRollingReserveCrypto merchantRollingReserveCrypto;
    @Autowired
    private GetMerchantRollingReserveAddressCrypto getMerchantRollingReserveAddressCrypto;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private Transactions transactions;
    @Autowired
    private Disputes disputes;

    private AtomicInteger lastAddressIndex;


    public void init() {
        lastAddressIndex = new AtomicInteger(ReservedAddress.values().length + 1); // Reserving initial values to predefined transactions
        merchantRollingReserves.forEach(c -> lastAddressIndex.getAndIncrement());
    }

    public Hash getCotiRollingReserveAddress() {
        return CryptoHelper.generateAddress(seed, COTI_ROLLING_RESERVE_ADDRESS_INDEX);
    }

    public synchronized int getNextAddressIndex() {
        if (lastAddressIndex == null) {
            init();
        }
        return lastAddressIndex.incrementAndGet();
    }

    public ResponseEntity getRollingReserveData(GetMerchantRollingReserveDataRequest request) {

        RollingReserveReleaseDateData rollingReserveReleaseDateData;
        RollingReserveReleaseStatus rollingReserveReleaseStatus;

        MerchantRollingReserveData merchantRollingReserveData = request.getMerchantRollingReserveData();

        if (!merchantRollingReserveCrypto.verifySignature(merchantRollingReserveData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNAUTHORIZED, STATUS_ERROR));
        }

        merchantRollingReserveData = getMerchantRollingReserveData(merchantRollingReserveData.getHash());

        Map<String, RollingReserveReleaseStatus> rollingReserveReleases = new HashMap<>();
        for (Date releaseData : merchantRollingReserveData.getReleaseDates()) {

            Hash dateHash = new Hash(releaseData.getTime());
            rollingReserveReleaseDateData = rollingReserveReleaseDates.getByHash(dateHash);
            rollingReserveReleaseStatus = rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().get(merchantRollingReserveData.getHash());

            rollingReserveReleases.put(releaseData.toString(), rollingReserveReleaseStatus);
        }

        RecourseClaimData recourseClaimData = recourseClaims.getByHash(merchantRollingReserveData.getMerchantHash());
        return ResponseEntity.status(HttpStatus.OK).body(new GetRollingReserveReleaseDatesResponse(merchantRollingReserveData, rollingReserveReleases, recourseClaimData));
    }


    public MerchantRollingReserveData getMerchantRollingReserveData(Hash merchantHash) {
        MerchantRollingReserveData merchantRollingReserveData = merchantRollingReserves.getByHash(merchantHash);
        if (merchantRollingReserveData == null) {
            createRollingReserveDataForMerchant(merchantHash);
            merchantRollingReserveData = merchantRollingReserves.getByHash(merchantHash);
            propagationPublisher.propagate(new MerchantRollingReserveAddressData(merchantRollingReserveData.getHash(), merchantRollingReserveData.getRollingReserveAddress()),
                    Arrays.asList(NodeType.TrustScoreNode));
        }
        return merchantRollingReserveData;
    }

    public ResponseEntity<IResponse> getMerchantRollingReserveAddress(GetMerchantRollingReserveAddressRequest request) {

        if (!getMerchantRollingReserveAddressCrypto.verifySignature(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new SerializableResponse(UNAUTHORIZED, STATUS_ERROR));
        }

        MerchantRollingReserveData merchantRollingReserveData = getMerchantRollingReserveData(request.getMerchantHash());
        return ResponseEntity.status(HttpStatus.OK).body(new GetMerchantRollingReserveAddressResponse(merchantRollingReserveData.getMerchantHash(), merchantRollingReserveData.getRollingReserveAddress()));
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

        if (!transactionHelper.getReceiverBaseTransactionAddressHash(transactionData).equals(getCotiRollingReserveAddress())) {
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

        try {
            // TODO: Get number of days from transaction data
            Date date = DatesHelper.getDateNumberOfDaysAfterToday(ROLLING_RESERVE_DEFAULT_DAYS_TO_HOLD);

            Hash dateHash = new Hash(date.getTime());
            RollingReserveReleaseDateData rollingReserveReleaseDateData = rollingReserveReleaseDates.getByHash(dateHash);

            if (rollingReserveReleaseDateData == null) {
                rollingReserveReleaseDateData = new RollingReserveReleaseDateData(date);
            }

            RollingReserveReleaseStatus rollingReserveReleaseStatus = rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().get(merchantHash);

            if (rollingReserveReleaseStatus == null) {
                rollingReserveReleaseStatus = new RollingReserveReleaseStatus(transactionHelper.getRollingReserveAmount(transactionData), transactionData.getHash());
            } else {
                rollingReserveReleaseStatus.addToInitialAmount(transactionHelper.getRollingReserveAmount(transactionData));
                rollingReserveReleaseStatus.getPaymentTransactions().add(transactionData.getHash());
            }

            rollingReserveReleaseDateData.getRollingReserveReleaseStatusByMerchant().put(merchantHash, rollingReserveReleaseStatus);

            if (merchantRollingReserves.getByHash(merchantHash) == null) {
                createRollingReserveDataForMerchant(merchantHash);
            }

            MerchantRollingReserveData merchantRollingReserveData = merchantRollingReserves.getByHash(merchantHash);

            rollingReserveReleaseDates.put(rollingReserveReleaseDateData);
            if (!merchantRollingReserveData.getReleaseDates().contains(date)) {
                merchantRollingReserveData.getReleaseDates().add(date);
            }

            merchantRollingReserves.put(merchantRollingReserveData);
            log.info("Rolling reserve release date set success for transaction {} and merchant {}", transactionData.getHash(), merchantHash);

        } catch (Exception e) {
            log.error("Rolling reserve release date set error for transaction {} and merchant {}", transactionData.getHash(), merchantHash);
            e.printStackTrace();
        }
    }

    public void chargebackConsumer(DisputeData disputeData, Hash consumerAddress, BigDecimal amount) {

        Hash dateHash;
        RollingReserveReleaseDateData rollingReserveReleaseDateData;
        Hash merchantHash = disputeData.getMerchantHash();

        if (merchantRollingReserves.getByHash(merchantHash) == null) {
            createRollingReserveDataForMerchant(merchantHash);
        }
        MerchantRollingReserveData merchantRollingReserveData = merchantRollingReserves.getByHash(merchantHash);
        List<Date> releaseDates = merchantRollingReserveData.getReleaseDates();

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
                    merchantRollingReserveData.getReleaseDates().remove(releaseDate);
                }

                remainingChargebackAmount = new BigDecimal(0);

                rollingReserveReleaseDates.put(rollingReserveReleaseDateData);
                break;
            }
        }

        transactionCreationService.createNewChargebackTransaction(amount, merchantRollingReserveData.getRollingReserveAddress(), consumerAddress, remainingChargebackAmount);

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

        merchantRollingReserves.put(merchantRollingReserveData);
    }

    private void createRollingReserveDataForMerchant(Hash merchantHash) {

        int addressIndex = getNextAddressIndex();
        Hash address = CryptoHelper.generateAddress(seed, addressIndex);

        MerchantRollingReserveData merchantRollingReserveData = new MerchantRollingReserveData();
        merchantRollingReserveData.setMerchantHash(merchantHash);
        merchantRollingReserveData.setRollingReserveAddress(address);
        merchantRollingReserveData.setAddressIndex(addressIndex);
        merchantRollingReserves.put(merchantRollingReserveData);
    }

    private void addConsumerToRollingReserveReceiver(RollingReserveReleaseStatus rollingReserveReleaseStatus) {

        if (rollingReserveReleaseStatus.getRollingReserveReceiver() == RollingReserveReceiver.Merchant) {
            rollingReserveReleaseStatus.setRollingReserveReceiver(RollingReserveReceiver.MerchantAndConsumer);
        } else {
            rollingReserveReleaseStatus.setRollingReserveReceiver(RollingReserveReceiver.Consumer);
        }
    }


}
