package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.NodeFeeData;
import io.coti.basenode.data.NodeFeeType;
import io.coti.basenode.http.NodeFeeSetRequest;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

public interface INodeFeesService {

    NodeFeeData getNodeFeeData(NodeFeeType nodeFeeType);

    void init(List<NodeFeeType> nodeFeeTypeList);

    BigDecimal calculateClassicFee(NodeFeeType nodeFeeType, BigDecimal amount);

    ResponseEntity<IResponse> setFeeValue(@Valid NodeFeeSetRequest nodeFeeSetRequest);

    ResponseEntity<IResponse> getNodeFees();
}
