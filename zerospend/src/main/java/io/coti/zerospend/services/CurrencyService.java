package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.CurrencyOriginatorCrypto;
import io.coti.basenode.crypto.CurrencyRegistrarCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeCurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class CurrencyService extends BaseNodeCurrencyService {

    @Value("${native.currency.name}")
    private String nativeCurrencyName;
    @Value("${native.currency.symbol}")
    private String nativeCurrencySymbol;
    @Value("${native.currency.supply}")
    private BigDecimal nativeCurrencyTotalSupply;
    @Value("${native.currency.scale}")
    private int nativeCurrencyScale;
    @Value("${native.currency.description}")
    private String nativeCurrencyDescription;

    @Autowired
    private CurrencyOriginatorCrypto currencyOriginatorCrypto;
    @Autowired
    private CurrencyRegistrarCrypto currencyRegistrarCrypto;
    @Autowired
    private ClusterStampService clusterStampService;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private NetworkService networkService;

    @Override
    public void updateCurrencies() {
        CurrencyData nativeCurrencyData = getNativeCurrency();
        if (nativeCurrencyData == null) {
            generateNativeCurrency();
        }
    }

    private void generateNativeCurrency() {
        CurrencyData currencyData = super.createCurrencyData(nativeCurrencyName, (nativeCurrencySymbol).toUpperCase(), nativeCurrencyTotalSupply, nativeCurrencyScale, Instant.now(), nativeCurrencyDescription
                , CurrencyType.NATIVE_COIN);
        putCurrencyData(currencyData);
        setNativeCurrencyData(currencyData);
    }

    public ResponseEntity<IResponse> initiateToken(CurrencyData currencyData) {
        if (!currencyOriginatorCrypto.verifySignature(currencyData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(String.format("Failed to verify currency originator %s", currencyData.getOriginatorHash().toString()), STATUS_ERROR));
        }
        if (!currencyRegistrarCrypto.verifySignature(currencyData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(String.format("Failed to verify currency %s", currencyData.getRegistrarHash().toString()), STATUS_ERROR));
        }
        NetworkNodeData financialServerNodeData = networkService.getSingleNodeData(NodeType.FinancialServer);
        if (!financialServerNodeData.getSignerHash().equals(currencyData.getRegistrarHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(String.format("Failed to verify currency %s request is from financial server", currencyData.getName()), STATUS_ERROR));
        }
        if (verifyCurrencyExists(currencyData.getHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(String.format("Currency name = %s already exists", currencyData.getName()), STATUS_ERROR));
        }
        if (currencyData.getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(String.format("Currency name = %s wrongly marked as Native", currencyData.getName()), STATUS_ERROR));
        }

        putCurrencyData(currencyData);

        ClusterStampNameData clusterStampNameData = clusterStampService.handleNewToken(currencyData);

        InitiatedTokenNoticeData initiatedTokenNoticeData = new InitiatedTokenNoticeData(currencyData, clusterStampNameData);
        propagationPublisher.propagate(initiatedTokenNoticeData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode));


        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response("Currency name = {} received successfully", currencyData.getName()));
    }

}