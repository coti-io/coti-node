package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.data.Fund;
import io.coti.financialserver.data.ReservedAddress;
import io.coti.financialserver.data.TokenSaleDistributionData;
import io.coti.financialserver.data.TokenSaleDistributionEntryData;
import io.coti.financialserver.http.TokenSaleDistributionRequest;
import io.coti.financialserver.http.TokenSaleDistributionResponse;
import io.coti.financialserver.http.data.TokenSaleDistributionResponseData;
import io.coti.financialserver.http.data.TokenSaleDistributionResultData;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.financialserver.http.HttpStringConstants.DISTRIBUTION_REQUEST_HANDLED_PREVIOUSLY;
import static io.coti.financialserver.http.HttpStringConstants.DUPLICATE_FUND_NAME;
import static io.coti.financialserver.services.NodeServiceManager.*;

@Slf4j
@Service
public class DistributeTokenService {

    @Value("${financialserver.seed.key:}")
    private String seed;
    @Value("${secret.financialserver.seed.name.key:}")
    private String seedSecretName;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;

    void init() {
        seed = secretManagerService.getSecret(seed, seedSecretName, "seed");
    }

    public ResponseEntity<IResponse> distributeTokens(TokenSaleDistributionRequest request) {

        TokenSaleDistributionData tokenSaleDistributionData = request.getTokenSaleDistributionData();
        tokenSaleDistributionData.init();

        EnumMap<Fund, Boolean> tokenSaleNameMap = new EnumMap<>(Fund.class);

        for (TokenSaleDistributionEntryData tokenSaleDistributionEntryData : tokenSaleDistributionData.getTokenDistributionDataEntries()) {
            if (tokenSaleNameMap.containsKey(tokenSaleDistributionEntryData.getFundName())) {
                return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(new Response(DUPLICATE_FUND_NAME, STATUS_ERROR));
            } else {
                tokenSaleNameMap.put(tokenSaleDistributionEntryData.getFundName(), true);
            }
        }

        if (!tokenSaleDistributionData.getSignerHash().toString().equals(kycServerPublicKey)) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body(new Response(INVALID_SIGNER, STATUS_ERROR));
        }
        if (!tokenSaleDistributionCrypto.verifySignature(tokenSaleDistributionData)) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        Hash tokenSaleDistributionDataHash = tokenSaleDistributionData.getHash();

        if (isDistributionDataPresentByHash(tokenSaleDistributionDataHash)) {
            return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(new Response(DISTRIBUTION_REQUEST_HANDLED_PREVIOUSLY, STATUS_ERROR));
        }

        tokenSaleDistributionData.getTokenDistributionDataEntries().forEach(this::distributeTokenByEntry);

        tokenSaleDistributions.put(tokenSaleDistributionData);

        return getDistributeTokensMultiResponse(tokenSaleDistributionData);
    }

    private ResponseEntity<IResponse> getDistributeTokensMultiResponse(TokenSaleDistributionData tokenSaleDistributionData) {
        List<TokenSaleDistributionResultData> tokenSaleDistributionResponseData = new ArrayList<>();

        tokenSaleDistributionData.getTokenDistributionDataEntries().forEach(entry ->
                tokenSaleDistributionResponseData.add(new TokenSaleDistributionResultData(entry.getFundName().getText(), entry.isCompletedSuccessfully()))
        );

        return ResponseEntity.status(HttpStatus.SC_OK)
                .body(new TokenSaleDistributionResponse(new TokenSaleDistributionResponseData(tokenSaleDistributionResponseData)));
    }

    private void distributeTokenByEntry(TokenSaleDistributionEntryData entry) {

        int tokenSaleIndex = Math.toIntExact(ReservedAddress.TOKEN_SALE.getIndex());
        Hash fundSourceAddress = nodeIdentityService.generateAddress(seed, tokenSaleIndex);
        int targetSaleIndex = Math.toIntExact(entry.getFundName().getReservedAddress().getIndex());
        Hash fundTargetAddress = nodeIdentityService.generateAddress(seed, targetSaleIndex);
        Hash transactionHash;
        try {
            transactionHash = transactionCreationService.createInitialTransaction(entry.getAmount(), currencyService.getNativeCurrencyHash(), fundSourceAddress, fundTargetAddress, tokenSaleIndex);
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            entry.setCompletedSuccessfully(false);
            return;
        }

        entry.setCompletedSuccessfully(true);
        entry.setTransactionHash(transactionHash);
    }

    private boolean isDistributionDataPresentByHash(Hash hash) {
        return tokenSaleDistributions.getByHash(hash) != null;
    }

}
