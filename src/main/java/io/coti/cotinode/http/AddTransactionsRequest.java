package io.coti.cotinode.http;

import lombok.Data;

import java.util.List;
@Data
public class AddTransactionsRequest extends Request {
    private List<AddTransactionRequest> transactionsRequest;
}
