package io.coti.common.http;

import java.util.Date;

public class GetTransactionsDataRequest extends Request {
    //(message = "If request all transactions, leave fromAttachmentTime null")
    public int lastIndex;
}
