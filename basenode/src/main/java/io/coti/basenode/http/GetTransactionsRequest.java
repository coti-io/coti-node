package io.coti.basenode.http;

import java.util.Date;

public class GetTransactionsRequest extends Request {
    //(message = "If request all transactions, leave fromAttachmentTime null")
    public Date fromAttachmentTime;
}
