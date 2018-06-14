package io.coti.cotinode.service;

import io.coti.cotinode.model.Balance;
import io.coti.cotinode.model.TransactionPackage;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class BalanceService {

    public double getBalance(byte[] addressHash) {
        return 12.54401;
    }

    public boolean isLegalTransaction(byte[] hash) {
        return true;
    }

    public void addToPreBalance(TransactionPackage transactionPackage) {
    }

    public void revertTransaction(TransactionPackage transactionPackage) {
    }

    public List<Double> getBalances(List<byte[]> addressHashes) {
        return Arrays.asList(-1.1);
    }
}
