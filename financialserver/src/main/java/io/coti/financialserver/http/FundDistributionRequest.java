package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.http.data.FundDistributionFileData;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class FundDistributionRequest {

    @NotEmpty
    String fileName;

    public FundDistributionRequest(String fileName) {
        this.fileName = fileName;
    }

    public FundDistributionFileData getFundDistributionFileData(Hash hash) {
        return new FundDistributionFileData(fileName, hash );
    }


}
