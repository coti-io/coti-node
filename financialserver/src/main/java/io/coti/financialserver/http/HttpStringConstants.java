package io.coti.financialserver.http;

import io.coti.basenode.http.BaseNodeHttpStringConstants;

public class HttpStringConstants extends BaseNodeHttpStringConstants {

    public static final String SUCCESS = "Success";

    public static final String DISPUTE_NOT_FOUND = "Dispute not found";
    public static final String DISPUTE_NOT_YOURS = "Dispute isn't yours";
    public static final String DISPUTE_ITEMS_EXIST_ALREADY = "At least one of the dispute items is already in process";

    public static final String DOCUMENT_NOT_FOUND = "Document not found";
    public static final String COMMENT_NOT_FOUND = "Comment not found";
    public static final String ITEM_NOT_FOUND = "Item not found";
    public static final String DISPUTE_PASSED_RECALL_STATUS = "Dispute passed recall status";

    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_ATTACHMENT_PREFIX = "attachment; filename=";
    public static final String HEADER_CONTENT_TYPE = "Content-Disposition";
    public static final String S3_SUFFIX_METADATA_KEY = "x-amz-meta-suffix";

    public static final String TRANSACTION_NOT_FOUND = "Transaction hash not found";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String NO_CONSUMER_HASH = "Consumer hash field is required";
    public static final String NO_MERCHANT_HASH = "Merchant hash field is required";
    public static final String INVALID_SIGNATURE = "Invalid signature";

    public static final String INTERNAL_ERROR = "Internal error";
}
