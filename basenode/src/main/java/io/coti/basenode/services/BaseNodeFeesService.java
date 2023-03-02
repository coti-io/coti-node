package io.coti.basenode.services;

import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.INodeFeesService;
import io.coti.basenode.services.interfaces.ITokenFeeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.coti.basenode.crypto.CryptoHelper.calculateTokenFeeHash;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.basenode.services.BaseNodeServiceManager.networkService;
import static io.coti.basenode.services.BaseNodeServiceManager.nodeFees;

@Slf4j
@Service
public class BaseNodeFeesService implements INodeFeesService {

    @Override
    public TokenFeeData getTokenFeeData(Hash tokenHash, NodeFeeType nodeFeeType) {
        Hash nodeFeeHash = calculateTokenFeeHash(tokenHash, nodeFeeType);
        return nodeFees.getByHash(nodeFeeHash);
    }

    private void deleteInvalidData() {
        if (!nodeFees.validate(Object::toString)) {
            nodeFees.deleteAll();
            log.warn("Deleted all entries of column family name nodeFees due to invalid data!");
        }
    }

    @Override
    public void init(List<TokenFeeData> tokenFeeDataList) {
        deleteInvalidData();
        for (TokenFeeData tokenFeeData : tokenFeeDataList) {
            Hash nodeFeeHash = tokenFeeData.getHash();
            TokenFeeData nodeFeeData = nodeFees.getByHash(nodeFeeHash);
            if (!tokenFeeData.valid()) {
                throw new IllegalArgumentException(String.format("Token %s, Type: %s is invalid!", tokenFeeData.getSymbol(), tokenFeeData.getNodeFeeType().toString()));
            }
            if (nodeFeeData == null) {
                nodeFees.put(tokenFeeData);
                log.info("setting up default ".concat(tokenFeeData.toString()));
            } else {
                log.info(tokenFeeData.toString());
            }
        }
        networkService.getNetworkNodeData().setTokenFees(tokenFeeDataList);
    }

    @Override
    public BigDecimal calculateClassicFee(Hash tokenHash, NodeFeeType nodeFeeType, BigDecimal amount) {
        TokenFeeData tokenFeeData = getTokenFeeData(tokenHash, nodeFeeType);
        if (tokenFeeData == null) {
            Hash defaultTokenHash = OriginatorCurrencyCrypto.calculateHash("*");
            tokenFeeData = getTokenFeeData(defaultTokenHash, nodeFeeType);
            if (tokenFeeData == null) {
                throw new CotiRunTimeException(String.format("no matching token found to calculate fee of type: %s", nodeFeeType.name()));
            }
        }

        return tokenFeeData.getFeeAmount(amount);
    }

    @Override
    @SuppressWarnings("java:S1612")
    public ResponseEntity<IResponse> getNodeFees() {

        ArrayList<ITokenFeeData> feesData = new ArrayList<>();
        nodeFees.forEach(fee -> feesData.add(fee));
        GetNodeFeesDataResponse getNodeFeesDataResponse = new GetNodeFeesDataResponse();
        getNodeFeesDataResponse.setNodeFeeDataArrayList(feesData);
        return ResponseEntity.status(HttpStatus.OK).body(getNodeFeesDataResponse);
    }

    @Override
    public ResponseEntity<IResponse> setFeeValue(@Valid RatioTokenFeeSetRequest ratioTokenFeeSetRequest) {

        FeeData feeData = new FeeData(ratioTokenFeeSetRequest.getFeePercentage(), ratioTokenFeeSetRequest.getFeeMinimum(),
                ratioTokenFeeSetRequest.getFeeMaximum());
        RatioTokenFeeData ratioTokenFeeData = new RatioTokenFeeData(ratioTokenFeeSetRequest.getTokenSymbol(),
                ratioTokenFeeSetRequest.getNodeFeeType(), feeData);
        Hash tokenHash = OriginatorCurrencyCrypto.calculateHash(ratioTokenFeeSetRequest.getTokenSymbol());
        if (ratioTokenFeeData.valid()) {
            TokenFeeData nodeFeeData = getTokenFeeData(tokenHash, ratioTokenFeeSetRequest.getNodeFeeType());
            if (nodeFeeData instanceof ConstantTokenFeeData) {
                nodeFees.deleteByHash(nodeFeeData.getHash());
            }
            nodeFees.put(ratioTokenFeeData);
            GetNodeFeesDataResponse getNodeFeesDataResponse = new GetNodeFeesDataResponse();
            getNodeFeesDataResponse.setNodeFeeDataArrayList(Collections.singletonList(ratioTokenFeeData));

            return ResponseEntity.status(HttpStatus.OK).body(getNodeFeesDataResponse);

        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_PARAMETERS_MESSAGE, STATUS_ERROR));
    }

    @Override
    public ResponseEntity<IResponse> setFeeValue(@Valid ConstantTokenFeeSetRequest constantTokenFeeSetRequest) {

        ConstantTokenFeeData constantTokenFeeData = new ConstantTokenFeeData(constantTokenFeeSetRequest.getTokenSymbol(),
                constantTokenFeeSetRequest.getNodeFeeType(), constantTokenFeeSetRequest.getConstant());
        Hash tokenHash = OriginatorCurrencyCrypto.calculateHash(constantTokenFeeSetRequest.getTokenSymbol());

        if (constantTokenFeeData.valid()) {
            TokenFeeData nodeFeeData = getTokenFeeData(tokenHash, constantTokenFeeSetRequest.getNodeFeeType());
            if (nodeFeeData instanceof RatioTokenFeeData) {
                nodeFees.deleteByHash(nodeFeeData.getHash());
            }
            nodeFees.put(constantTokenFeeData);
            GetNodeFeesDataResponse getNodeFeesDataResponse = new GetNodeFeesDataResponse();
            getNodeFeesDataResponse.setNodeFeeDataArrayList(Collections.singletonList(constantTokenFeeData));

            return ResponseEntity.status(HttpStatus.OK).body(getNodeFeesDataResponse);

        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_PARAMETERS_MESSAGE, STATUS_ERROR));
    }

    @Override
    public ResponseEntity<IResponse> deleteFeeValue(DeleteTokenFeeRequest deleteTokenFeeRequest) {

        Hash tokenHash = OriginatorCurrencyCrypto.calculateHash(deleteTokenFeeRequest.getTokenSymbol());
        TokenFeeData nodeFeeData = getTokenFeeData(tokenHash, deleteTokenFeeRequest.getNodeFeeType());
        if (nodeFeeData != null) {
            nodeFees.deleteByHash(nodeFeeData.getHash());
            GetNodeFeesDataResponse getNodeFeesDataResponse = new GetNodeFeesDataResponse();
            getNodeFeesDataResponse.setNodeFeeDataArrayList(Collections.singletonList(nodeFeeData));

            return ResponseEntity.status(HttpStatus.OK).body(getNodeFeesDataResponse);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_FEE_NOT_FOUND, STATUS_ERROR));
    }
}
