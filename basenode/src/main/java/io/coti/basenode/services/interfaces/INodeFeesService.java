package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeFeeType;
import io.coti.basenode.data.TokenFeeData;
import io.coti.basenode.http.ConstantTokenFeeSetRequest;
import io.coti.basenode.http.DeleteTokenFeeRequest;
import io.coti.basenode.http.RatioTokenFeeSetRequest;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

public interface INodeFeesService {

    TokenFeeData getTokenFeeData(Hash tokenHash, NodeFeeType nodeFeeType);

    void init(List<TokenFeeData> tokenFeeDataList);

    BigDecimal calculateClassicFee(Hash tokenHash, NodeFeeType nodeFeeType, BigDecimal amount);

    ResponseEntity<IResponse> setFeeValue(@Valid ConstantTokenFeeSetRequest constantTokenFeeSetRequest);

    ResponseEntity<IResponse> setFeeValue(@Valid RatioTokenFeeSetRequest ratioTokenFeeSetRequest);

    ResponseEntity<IResponse> deleteFeeValue(@Valid DeleteTokenFeeRequest deleteTokenFeeRequest);

    ResponseEntity<IResponse> getNodeFees();

}
