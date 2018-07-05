package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class GetTransactionsRequest extends Request {
    //(message = "If request all transactions, leave fromAttachmentTime null")
    public Date fromAttachmentTime;
}
