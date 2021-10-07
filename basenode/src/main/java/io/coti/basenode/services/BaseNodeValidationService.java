package io.coti.basenode.services;

import io.coti.basenode.crypto.*;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IPotService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumSet;

import static io.coti.basenode.services.BaseNodeTransactionHelper.CURRENCY_SCALE;


@Service
public class BaseNodeValidationService implements IValidationService {

    @Autowired
    private Transactions transactions;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private TransactionSenderCrypto transactionSenderCrypto;
    @Autowired
    private IPotService potService;
    @Autowired
    private InvalidTransactionCrypto invalidTransactionCrypto;


    @Override
    public boolean validateSource(Hash transactionHash) {
        if (transactionHash != null) {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            if (transactionData != null) {
                transactionData.setValid(true);
                transactions.put(transactionData);
            }
        }
        return true;
    }

    @Override
    public boolean validateAddress(Hash address) {

        return CryptoHelper.isAddressValid(address);
    }

    @Override
    public boolean validateTransactionDataIntegrity(TransactionData transactionData) {
        return transactionHelper.validateTransactionType(transactionData) && transactionHelper.validateTransactionCrypto(transactionData)
                && transactionHelper.validateBaseTransactionsDataIntegrity(transactionData) && validateTransactionSenderSignature(transactionData);
    }

    @Override
    public boolean validatePropagatedTransactionDataIntegrity(TransactionData transactionData) {
        return validateTransactionDataIntegrity(transactionData) && validateTransactionNodeSignature(transactionData) &&
                //validateTransactionTrustScore(transactionData) &&
                validateBaseTransactionAmounts(transactionData) && validatePot(transactionData);
    }

    @Override
    public boolean validatePropagatedInvalidTransactionDataIntegrity(InvalidTransactionData invalidTransactionData) {
        return invalidTransactionCrypto.verifySignature(invalidTransactionData);
    }

    @Override
    public boolean validateTransactionNodeSignature(TransactionData transactionData) {
        return transactionCrypto.verifySignature(transactionData);
    }

    @Override
    public boolean validateTransactionSenderSignature(TransactionData transactionData) {
        return EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial).contains(transactionData.getType()) || transactionSenderCrypto.verifySignature(transactionData);
    }

    @Override
    public boolean validateTransactionTrustScore(TransactionData transactionData) {
        return transactionHelper.validateTrustScore(transactionData);
    }

    @Override
    public boolean validateBaseTransactionAmounts(TransactionData transactionData) {
        return transactionHelper.validateBaseTransactionAmounts(transactionData.getBaseTransactions());
    }

    @Override
    public boolean validateBalancesAndAddToPreBalance(TransactionData transactionData) {
        return transactionHelper.checkBalancesAndAddToPreBalance(transactionData);
    }

    @Override
    public <T extends BaseTransactionData & ITrustScoreNodeValidatable> boolean validateBaseTransactionTrustScoreNodeResult(T baseTransactionData) {
        return transactionHelper.validateBaseTransactionTrustScoreNodeResult(baseTransactionData);
    }

    @Override
    public boolean fullValidation(TransactionData transactionData) {
        return true;
    }


    @Override
    public boolean validatePot(TransactionData transactionData) {
        return EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial).contains(transactionData.getType()) || potService.validatePot(transactionData);
    }

    @Override
    public boolean validateTransactionTimeFields(TransactionData transactionData) {
        return transactionHelper.validateTransactionTimeFields(transactionData);
    }

    @Override
    public boolean validateAmountField(BigDecimal amount) {
        return amount != null && (amount.scale() <= 0 || amount.stripTrailingZeros().equals(amount)) && amount.scale() <= CURRENCY_SCALE;
    }

    @Override
    public boolean validateGetAddressesResponse(GetHistoryAddressesResponse getHistoryAddressesResponse) {
        return getHistoryAddressesResponse.getAddressHashesToAddresses().entrySet().stream().noneMatch(entry ->
                entry.getValue() != null && !entry.getKey().equals(entry.getValue().getHash())
        );
    }

}
