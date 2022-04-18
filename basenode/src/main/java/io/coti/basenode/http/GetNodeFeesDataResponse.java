package io.coti.basenode.http;

import io.coti.basenode.data.NodeFeeData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNodeFeesDataResponse extends BaseResponse {

    private List<NodeFeeData> nodeFeeDataArrayList;


}
