package io.coti.financialserver.http;

import io.coti.basenode.http.BaseNodeHttpStringConstants;

public class HttpStringConstants extends BaseNodeHttpStringConstants {

    public static final String DISPUTE_NOT_FOUND = "Dispute not found";
    public static final String DISPUTE_NOT_YOURS = "Dispute isn't yours";
    public static final String TRANSACTION_NOT_FOUND = "Transaction hash not found";
    public static final String DISPUTE_ITEMS_EXIST_ALREADY = "At least one of the dispute items is already in process";

    public static final String NO_CONSUMER_HASH = "Consumer hash field is required";
    public static final String NO_MERCHANT_HASH = "Merchant hash field is required";
    public static final String INVALID_SIGNATURE = "Invalid signature";
}
