package io.coti.trustscore.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.GetMerchantRollingReserveAddressCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.http.GetMerchantRollingReserveAddressRequest;
import io.coti.basenode.http.GetMerchantRollingReserveAddressResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.http.RollingReserveRequest;
import io.coti.trustscore.http.RollingReserveResponse;
import io.coti.trustscore.http.RollingReserveValidateRequest;
import io.coti.trustscore.http.data.RollingReserveResponseData;
import io.coti.trustscore.model.MerchantRollingReserveAddresses;
import io.coti.trustscore.model.TrustScores;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.trustscore.http.HttpStringConstants.*;

@Slf4j
@Service
public class RollingReserveService {
    private static final double MAX_ROLLING_RESERVE_RATE = 100;
    private static final String MERCHANT_ADDRESS_END_POINT = "/rollingReserve/merchantReserveAddress";
    @Autowired
    private NetworkFeeService feeService;
    @Value("${rolling.reserve.difference.validation}")
    private BigDecimal rollingReserveDifferenceValidation;
    @Value("${financial.server.address}")
    private String financialServerAddress;
    @Autowired
    private GetMerchantRollingReserveAddressCrypto getMerchantRollingReserveAddressCrypto;
    @Autowired
    private MerchantRollingReserveAddresses merchantRollingReserveAddresses;
    @Autowired
    private TrustScores trustScores;


    @Autowired
    private TrustScoreService trustScoreService;

    public ResponseEntity<IResponse> createRollingReserveFee(RollingReserveRequest rollingReserveRequest) {

        try {

            NetworkFeeData networkFeeData = rollingReserveRequest.getNetworkFeeData();
            if (!feeService.validateNetworkFee(networkFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(NETWORK_FEE_VALIDATION_ERROR, STATUS_ERROR));
            }

            TrustScoreData trustScoreData = trustScores.getByHash(rollingReserveRequest.getMerchantHash());

            if (!trustScoreData.getUserType().equals(UserType.MERCHANT)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(String.format(USER_NOT_MERCHANT, rollingReserveRequest.getMerchantHash()), STATUS_ERROR));
            }

            BigDecimal originalAmount = networkFeeData.getOriginalAmount();
            BigDecimal reducedAmount = networkFeeData.getReducedAmount().subtract(networkFeeData.getAmount());

            Hash rollingReserveAddress = getMerchantRollingReserveAddress(rollingReserveRequest.getMerchantHash());
            BigDecimal rollingReserveAmount = calculateRollingReserveAmount(reducedAmount, trustScoreService.calculateUserTrustScore(trustScoreData));

            RollingReserveData rollingReserveData = new RollingReserveData(rollingReserveAddress, rollingReserveAmount, originalAmount, reducedAmount, new Date());
            setRollingReserveNodeFeeHash(rollingReserveData);
            signRollingReserveFee(rollingReserveData, true);
            RollingReserveResponseData rollingReserveResponseData = new RollingReserveResponseData(rollingReserveData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RollingReserveResponse(rollingReserveResponseData));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    public Hash getMerchantRollingReserveAddress(Hash merchantHash) throws Exception {
        MerchantRollingReserveAddressData merchantRollingReserveAddressData = merchantRollingReserveAddresses.getByHash(merchantHash);
        if (merchantRollingReserveAddressData == null) {
            merchantRollingReserveAddressData = getMerchantAddressFromFinancialNode(merchantHash);
            merchantRollingReserveAddresses.put(merchantRollingReserveAddressData);
        }

        return merchantRollingReserveAddressData.getMerchantRollingReserveAddress();
    }

    private MerchantRollingReserveAddressData getMerchantAddressFromFinancialNode(Hash merchantHash) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        GetMerchantRollingReserveAddressRequest getMerchantRollingReserveAddressRequest = new GetMerchantRollingReserveAddressRequest();
        getMerchantRollingReserveAddressRequest.setMerchantHash(merchantHash);

        getMerchantRollingReserveAddressCrypto.signMessage(getMerchantRollingReserveAddressRequest);

        ResponseEntity<GetMerchantRollingReserveAddressResponse> result = restTemplate.postForEntity(financialServerAddress + MERCHANT_ADDRESS_END_POINT, getMerchantRollingReserveAddressRequest, GetMerchantRollingReserveAddressResponse.class);

        if (!result.getStatusCode().equals(HttpStatus.OK)) {
            throw new Exception(String.format(MERCHANT_ADRRESS_GET_ERROR, result.getBody().getMessage()));
        }
        return result.getBody().getMerchantRollingReserveAddressData();
    }

    public ResponseEntity<IResponse> validateRollingReserve(RollingReserveValidateRequest rollingReserveValidateRequest) {
        try {

            NetworkFeeData networkFeeData = rollingReserveValidateRequest.getNetworkFeeData();
            if (!feeService.validateNetworkFee(networkFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(NETWORK_FEE_VALIDATION_ERROR, STATUS_ERROR));
            }
            TrustScoreData trustScoreData = trustScores.getByHash(rollingReserveValidateRequest.getMerchantHash());

            RollingReserveData rollingReserveData = rollingReserveValidateRequest.getRollingReserveData();
            boolean isValid = isRollingReserveValid(rollingReserveData, networkFeeData, trustScoreService.calculateUserTrustScore(trustScoreData), trustScoreData.getUserType());
            signRollingReserveFee(rollingReserveData, isValid);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RollingReserveResponse(new RollingReserveResponseData(rollingReserveData)));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public void setRollingReserveNodeFeeHash(RollingReserveData rollingReserveData) throws ClassNotFoundException {
        BaseTransactionCrypto.RollingReserveData.setBaseTransactionHash(rollingReserveData);
    }

    public void signRollingReserveFee(RollingReserveData rollingReserveData, boolean isValid) throws ClassNotFoundException {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(rollingReserveData);
        BaseTransactionCrypto.RollingReserveData.signMessage(new TransactionData(baseTransactions), rollingReserveData, new TrustScoreNodeResultData(NodeCryptoHelper.getNodeHash(), isValid));
    }

    private boolean isRollingReserveValid(RollingReserveData rollingReserveData, NetworkFeeData networkFeeData, double userTrustScore, UserType userType) {
        return userType.equals(UserType.MERCHANT) && rollingReserveData.getReducedAmount().equals(networkFeeData.getReducedAmount().subtract(networkFeeData.getAmount()))
                && isRollingReserveValid(rollingReserveData, userTrustScore);
    }

    private boolean isRollingReserveValid(RollingReserveData rollingReserveData, double userTrustScore) {
        BigDecimal calculatedReserve = calculateRollingReserveAmount(rollingReserveData.getReducedAmount(), userTrustScore);
        int compareResult = rollingReserveDifferenceValidation.compareTo(calculatedReserve.subtract(rollingReserveData.getAmount()).abs());
        return compareResult >= 0 && validateRollingReserveCrypto(rollingReserveData);
    }

    private boolean validateRollingReserveCrypto(RollingReserveData rollingReserveData) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(rollingReserveData);
        return BaseTransactionCrypto.RollingReserveData.isBaseTransactionValid(new TransactionData(baseTransactions), rollingReserveData);
    }

    private BigDecimal calculateRollingReserveAmount(BigDecimal reducedAmount, double trustScore) {

        double reserveRate = (trustScore == 0) ? MAX_ROLLING_RESERVE_RATE : Math.min(MAX_ROLLING_RESERVE_RATE / trustScore, MAX_ROLLING_RESERVE_RATE);
        return reducedAmount.multiply(new BigDecimal(reserveRate / 100));
    }
}
