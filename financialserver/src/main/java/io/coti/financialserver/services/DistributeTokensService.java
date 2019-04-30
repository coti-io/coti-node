package io.coti.financialserver.services;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.financialserver.crypto.TokenSaleDistributionCrypto;
import io.coti.financialserver.data.ReservedAddress;
import io.coti.financialserver.data.TokenSale;
import io.coti.financialserver.data.TokenSaleDistributionData;
import io.coti.financialserver.data.TokenSaleDistributionEntryData;
import io.coti.financialserver.http.TokenSaleDistributionRequest;
import io.coti.financialserver.http.TokenSaleDistributionResponse;
import io.coti.financialserver.http.data.TokenSaleDistributionResponseData;
import io.coti.financialserver.model.TokenSaleDistributions;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class DistributeTokensService {

    private static final String DISTRIBUTION_REQUEST_HANDLED_PREVIOUSLY = "Distribution request handled previously";

    private static final int COTI_GENESIS_ADDRESS_INDEX = Math.toIntExact(ReservedAddress.GENESIS_ONE.getIndex());

    @Value("${financialserver.seed}")
    private String seed;

    @Autowired
    TokenSaleDistributionCrypto tokenSaleDistributionCrypto;
    @Autowired
    TransactionCreationService transactionCreationService;
    @Autowired
    Transactions transactions;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private TokenSaleDistributions tokenSaleDistributions;

    public ResponseEntity<IResponse> distributeTokens(TokenSaleDistributionRequest request) {

        @NotNull @Valid TokenSaleDistributionData tokenSaleDistributionData = request.getTokenSaleDistributionData();
        // Validate signature
        boolean verifySignature = tokenSaleDistributionCrypto.verifySignature(tokenSaleDistributionData);
//        verifySignature = true; // TODO: Temp line for testing purposes
        if( !verifySignature ) {
            return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        // Validate if TokenSaleDistributionData according to its HashCode is not already in DB
        Hash tokenSaleDistributionDataHash = tokenSaleDistributionData.getHash();
        boolean distributionDataPreviouslyHandled = isDistributionDataPresentByHash(tokenSaleDistributionDataHash);
        if( distributionDataPreviouslyHandled ) {
            return ResponseEntity.status(HttpStatus.SC_CONFLICT).body(new Response(DISTRIBUTION_REQUEST_HANDLED_PREVIOUSLY, STATUS_ERROR));
        }

        // For each entry in the TokenSaleDistributionData create a new Transaction of type Initial
        tokenSaleDistributionData.getTokenDistributionDataEntries().forEach(entry -> distributeTokenByEntry(entry));

        // Create entry in DB for TokenSaleDistributionData, which was handled
        tokenSaleDistributions.put(tokenSaleDistributionData);

        // Return a response with a list of success \ failure per expected transaction
        return getDistributeTokensMultiResponse(tokenSaleDistributionData);
    }

    private ResponseEntity<IResponse> getDistributeTokensMultiResponse(TokenSaleDistributionData tokenSaleDistributionData) {
        Map<String, String> tokenSaleDistributionResponseData = new HashMap<>();

        tokenSaleDistributionData.getTokenDistributionDataEntries().forEach(entry -> {
            tokenSaleDistributionResponseData.put(entry.getFundName(), String.valueOf(entry.isCompletedSuccessfully() ? HttpStatus.SC_ACCEPTED : HttpStatus.SC_NOT_ACCEPTABLE));
        });

        return ResponseEntity.status(HttpStatus.SC_OK)
                .body(new TokenSaleDistributionResponse(new TokenSaleDistributionResponseData(tokenSaleDistributionResponseData)));
    }

    private void distributeTokenByEntry(TokenSaleDistributionEntryData entry) {
        long indexByText = TokenSale.getIndexByText(entry.getFundName());
        if( indexByText < 0 ) {
            entry.setCompletedSuccessfully(false);
            log.info("Failed to create transaction to: {}, unrecognized fund.", entry.getFundName());
            return;
        }
        Hash fundSourceAddress = nodeCryptoHelper.generateAddress(seed, Math.toIntExact(ReservedAddress.TOKEN_SELL.getIndex()));
        Hash fundTargetAddress = nodeCryptoHelper.generateAddress(seed, Math.toIntExact(indexByText));
        Hash transactionHash = transactionCreationService.createInitialTransactionToFund(entry.getAmount(), fundSourceAddress , fundTargetAddress, COTI_GENESIS_ADDRESS_INDEX);

        entry.setCompletedSuccessfully(true);
        entry.setDistributionHash(transactionHash);
    }

    private boolean isDistributionDataPresentByHash(Hash hash) {
        TokenSaleDistributionData tokenSaleDistributionsByHash = tokenSaleDistributions.getByHash(hash);
        return tokenSaleDistributionsByHash != null;
    }

    //TODO: temp code below for internal testing
//
//    public void testTokensDistribution() {
//        TokenSaleDistributionData tokenSaleDistributionData = new TokenSaleDistributionData();
//        SignatureData signature = new SignatureData();
//        signature.setR("1123785d7f0303aa1f17e63675e54b00b85560896c28cb2cb76917cf0f068581");
//        signature.setS("90ed9134e83e43bb6fa094ee3d97cf561aeb49297c93bb9243d3abe6e3a3cb9");
//        tokenSaleDistributionData.setSignature(signature);
//        Hash signerHash = generateRandomHash();
//        tokenSaleDistributionData.setSignerHash(signerHash);
//
//
//        List<TokenSaleDistributionEntryData> tokenSaleDistributionEntries = new ArrayList<TokenSaleDistributionEntryData>();
//        String fundName1 = "Private Sale";
//        BigDecimal amount1 = new BigDecimal(1000);
//        String identify1 = "Description one";
//        TokenSaleDistributionEntryData tokenSaleDistEntryData1 = new TokenSaleDistributionEntryData(fundName1, amount1, identify1);
//        tokenSaleDistributionEntries.add(tokenSaleDistEntryData1);
//
//        String fundName2 = "Equity Investors";
//        BigDecimal amount2 = new BigDecimal(2000);
//        String identify2 = "Description two";
//        TokenSaleDistributionEntryData tokenSaleDistEntryData2 = new TokenSaleDistributionEntryData(fundName2, amount2, identify2);
//        tokenSaleDistributionEntries.add(tokenSaleDistEntryData2);
//
//        tokenSaleDistributionEntries.add( new TokenSaleDistributionEntryData("No Fund", new BigDecimal(3000), "Description three"));
//
//        tokenSaleDistributionData.setTokenDistributionDataEntries(tokenSaleDistributionEntries);
//
//        TokenSaleDistributionRequest request = new TokenSaleDistributionRequest();
//        request.setTokenSaleDistributionData(tokenSaleDistributionData);
//        tokenSaleDistributionData.init();
//        ResponseEntity responseEntity = distributeTokens(request);
//
//        int iPause = 7;
//
//    }
//
//    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
//    private static final int SIZE_OF_HASH = 64;
//
//    public static Hash generateRandomHash() {
//        return generateRandomHash(SIZE_OF_HASH);
//    }
//
//    public static Hash generateRandomHash(int lengthOfHash) {
//        StringBuilder hexa = new StringBuilder();
//        for (int i = 0; i < lengthOfHash; i++) {
//            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
//            hexa.append(hexaOptions[randomNum]);
//        }
//        return new Hash(hexa.toString());
//    }
}
