package io.coti.financialserver.http;

import io.coti.basenode.http.BaseNodeHttpStringConstants;

public class HttpStringConstants extends BaseNodeHttpStringConstants {

    public static final String SUCCESS = "Success";

    public static final String DISPUTE_ALREADY_EXISTS_FOR_TRANSACTION = "Dispute already exists for this transaction";
    public static final String DISPUTE_COMMENT_CREATE_UNAUTHORIZED = "Unauthorized dispute comment creation request";
    public static final String DISPUTE_COMMENT_UNAUTHORIZED = "Unauthorized dispute comment request";
    public static final String DISPUTE_DOCUMENT_CREATE_UNAUTHORIZED = "Unauthorized dispute document creation request";
    public static final String DISPUTE_DOCUMENT_UNAUTHORIZED = "Unauthorized dispute document request";
    public static final String DISPUTE_MERCHANT_NOT_FOUND = "Merchant not found";
    public static final String DISPUTE_NOT_FOUND = "Dispute not found";
    public static final String DISPUTE_UNAUTHORIZED = "Unauthorized dispute request";
    public static final String DISPUTE_ITEM_NOT_FOUND = "Dispute item not found";
    public static final String DISPUTE_ITEM_STATUS_FINAL = "Dispute item status is final. It can not be changed";
    public static final String DISPUTE_ITEM_STATUS_INVALID_CHANGE = "Dispute item status change is invalid";
    public static final String DISPUTE_ITEM_STATUS_INVALID_ACTIONSIDE = "%s is not valid side for item status change to %s";
    public static final String DISPUTE_ITEM_UPDATE_SUCCESS = "Dispute item successfully updated";
    public static final String DISPUTE_ITEM_VOTE_SUCCESS = "Dispute item vote successfully submitted";
    public static final String DISPUTE_ITEMS_EXIST_ALREADY = "At least one of the dispute items is already was(or right now) in dispute";
    public static final String DISPUTE_ITEMS_INVALID = "Invalid dispute items";
    public static final String DISPUTE_STATUS_FINAL = "Dispute status is final. It can not be changed";
    public static final String DISPUTE_STATUS_INVALID_CHANGE = "Dispute status change is invalid";
    public static final String DISPUTE_TRANSACTION_NOT_FOUND = "Transaction hash not found";
    public static final String DISPUTE_TRANSACTION_NOT_PAYMENT = "Transaction is not of type payment";
    public static final String DISPUTE_TRANSACTION_SENDER_INVALID = "Invalid transaction sender";
    public static final String OPEN_DISPUTE_IN_PROCESS_FOR_THIS_TRANSACTION = "Open dispute already in process for this transaction";

    public static final String DOCUMENT_EXISTS_ERROR = "Document already exists";
    public static final String DOCUMENT_NOT_FOUND = "Document not found";
    public static final String DOCUMENT_UPLOAD_ERROR = "Some error occurred during upload";
    public static final String COMMENT_NOT_FOUND = "Comment not found";
    public static final String ITEM_NOT_FOUND = "Item not found";
    public static final String DISPUTE_ITEM_PASSED_RECALL_STATUS = "Dispute item passed recall status";
    public static final String DISPUTE_NOT_IN_CLAIM_STATUS = "Dispute not in claim status";
    public static final String ALREADY_GOT_YOUR_VOTE = "You already voted on this item";

    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_ATTACHMENT_PREFIX = "attachment; filename=";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String S3_SUFFIX_METADATA_KEY = "x-amz-meta-suffix";

    public static final String NOT_COTI_POOL = "Transaction receiver isn't Coti pool";
    public static final String ALREADY_GOT_THIS_RECOURSE_CLAIM = "Recourse claim transaction already processed";
    public static final String NOT_ENOUGH_MONEY_IN_TRANSACTION = "Not enough money in transaction";

    public static final String INTERNAL_ERROR = "Internal error";


}
