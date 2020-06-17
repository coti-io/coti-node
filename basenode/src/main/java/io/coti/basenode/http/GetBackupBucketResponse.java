package io.coti.basenode.http;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetBackupBucketResponse extends BaseResponse {

    private String backupBucket;

    private GetBackupBucketResponse() {
    }

    public GetBackupBucketResponse(String backupBucket) {
        this.backupBucket = backupBucket;
    }
}
