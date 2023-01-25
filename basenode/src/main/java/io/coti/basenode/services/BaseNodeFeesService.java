package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.FeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeFeeData;
import io.coti.basenode.data.NodeFeeType;
import io.coti.basenode.http.GetNodeFeesDataResponse;
import io.coti.basenode.http.NodeFeeSetRequest;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.INodeFeesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_PARAMETERS_MESSAGE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.services.BaseNodeServiceManager.nodeFees;
import static io.coti.basenode.services.BaseNodeTransactionHelper.CURRENCY_SCALE;

@Slf4j
@Service
public class BaseNodeFeesService implements INodeFeesService {

    @Value("${fee.percentage.default:10}")
    private BigDecimal defaultFeePercentage;
    @Value("${fee.minimum.default:1}")
    private BigDecimal defaultMinimumFee;
    @Value("${fee.maximum.default:25}")
    private BigDecimal defaultMaximumFee;

    @Override
    public NodeFeeData getNodeFeeData(NodeFeeType nodeFeeType) {
        Hash nodeFeeHash = CryptoHelper.cryptoHash(nodeFeeType.name().getBytes(StandardCharsets.UTF_8));
        return nodeFees.getByHash(nodeFeeHash);
    }

    @Override
    public void init(List<NodeFeeType> nodeFeeTypeList) {
        for (NodeFeeType nodeFeeType : nodeFeeTypeList) {
            Hash nodeFeeHash = nodeFeeType.getHash();
            NodeFeeData nodeFeeData = nodeFees.getByHash(nodeFeeHash);
            if (nodeFeeData == null) {
                FeeData feeData = new FeeData(defaultFeePercentage, defaultMinimumFee, defaultMaximumFee);
                nodeFeeData = new NodeFeeData(nodeFeeType, feeData);
                nodeFees.put(nodeFeeData);
                log.info("setting up default values for fee: {} , percentage: {}, minimum {}, maximum {} ", nodeFeeType.name(),
                        defaultFeePercentage, defaultMinimumFee, defaultMaximumFee);
            } else {
                log.info("values for fee: {} , percentage: {}, minimum {}, maximum {} ", nodeFeeData.getNodeFeeType().name(),
                        nodeFeeData.getFeeData().getFeePercentage(), nodeFeeData.getFeeData().getMinimumFee(), nodeFeeData.getFeeData().getMaximumFee());
            }
        }
    }

    @Override
    public BigDecimal calculateClassicFee(NodeFeeType nodeFeeType, BigDecimal amount) {
        NodeFeeData nodeFeeData = getNodeFeeData(nodeFeeType);

        BigDecimal feeAmount;
        BigDecimal fee = amount.multiply(nodeFeeData.getFeeData().getFeePercentage()).divide(new BigDecimal(100));
        if (fee.compareTo(nodeFeeData.getFeeData().getMinimumFee()) <= 0) {
            feeAmount = nodeFeeData.getFeeData().getMinimumFee();
        } else if (fee.compareTo(nodeFeeData.getFeeData().getMaximumFee()) >= 0) {
            feeAmount = nodeFeeData.getFeeData().getMaximumFee();
        } else {
            feeAmount = fee;
        }
        if (feeAmount.scale() > CURRENCY_SCALE) {
            feeAmount = feeAmount.setScale(CURRENCY_SCALE, RoundingMode.DOWN);
        }
        if (feeAmount.scale() > 0) {
            feeAmount = feeAmount.stripTrailingZeros();
        }

        return feeAmount;
    }

    private boolean newFeeValuesValid(NodeFeeSetRequest nodeFeeSetRequest) {
        return !(nodeFeeSetRequest.getFeePercentage().compareTo(BigDecimal.ZERO) <= 0 ||
                nodeFeeSetRequest.getFeePercentage().compareTo(BigDecimal.valueOf(100)) > 0 ||
                nodeFeeSetRequest.getFeeMinimum().compareTo(BigDecimal.ZERO) <= 0 ||
                nodeFeeSetRequest.getFeeMaximum().compareTo(nodeFeeSetRequest.getFeeMinimum()) < 0);
    }

    @Override
    public ResponseEntity<IResponse> setFeeValue(@Valid NodeFeeSetRequest nodeFeeSetRequest) {

        if (newFeeValuesValid(nodeFeeSetRequest)) {
            NodeFeeType nodeFeeType = NodeFeeType.valueOf(nodeFeeSetRequest.getNodeFeeType().toString());
            NodeFeeData nodeFeeData = nodeFees.getByHash(nodeFeeType.getHash());
            if (nodeFeeData != null) {
                FeeData feeData = nodeFeeData.getFeeData();
                feeData.setFeePercentage(nodeFeeSetRequest.getFeePercentage());
                feeData.setMinimumFee(nodeFeeSetRequest.getFeeMinimum());
                feeData.setMaximumFee(nodeFeeSetRequest.getFeeMaximum());
                nodeFeeData.setFeeData(feeData);
                nodeFees.put(nodeFeeData);
                GetNodeFeesDataResponse getNodeFeesDataResponse = new GetNodeFeesDataResponse();
                getNodeFeesDataResponse.setNodeFeeDataArrayList(Collections.singletonList(nodeFeeData));

                return ResponseEntity.status(HttpStatus.OK).body(getNodeFeesDataResponse);
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_PARAMETERS_MESSAGE, STATUS_ERROR));
    }

    @Override
    @SuppressWarnings("java:S1612")
    public ResponseEntity<IResponse> getNodeFees() {

        ArrayList<NodeFeeData> feesData = new ArrayList<>();
        nodeFees.forEach(fee -> feesData.add(fee));
        GetNodeFeesDataResponse getNodeFeesDataResponse = new GetNodeFeesDataResponse();
        getNodeFeesDataResponse.setNodeFeeDataArrayList(feesData);
        return ResponseEntity.status(HttpStatus.OK).body(getNodeFeesDataResponse);
    }
}
