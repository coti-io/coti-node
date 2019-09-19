package io.coti.basenode.http;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GetTokenGenerationDataResponse extends BaseResponse {

    @NotNull
    private Map<Hash, CurrencyData> completedTransactionHashToGeneratedCurrency;
    @NotNull
    private Map<Hash, CurrencyData> pendingTransactionHashToGeneratedCurrency;
    @NotNull
    private List<Hash> unusedConfirmedTransactions;

    public GetTokenGenerationDataResponse(){
        completedTransactionHashToGeneratedCurrency = new HashMap<>();
        pendingTransactionHashToGeneratedCurrency = new HashMap<>();
        unusedConfirmedTransactions = new ArrayList<>();
    }

    public void addUnusedConfirmedTransaction(Hash transactionHash){
        unusedConfirmedTransactions.add(transactionHash);
    }

    public void addPendingTransactionHashToGeneratedCurrency(Hash transactionHash, CurrencyData currencyData){
        pendingTransactionHashToGeneratedCurrency.put(transactionHash,currencyData);
    }

    public void addCompletedTransactionHashToGeneratedCurrency(Hash transactionHash,CurrencyData currencyData){
        completedTransactionHashToGeneratedCurrency.put(transactionHash, currencyData);
    }

}
