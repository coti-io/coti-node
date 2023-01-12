package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.services.interfaces.IValidationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumSet;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static io.coti.basenode.services.BaseNodeTransactionHelper.CURRENCY_SCALE;

@Service
public class BaseNodeValidationService implements IValidationService {

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
        return nodeTransactionHelper.validateTransactionType(transactionData) && nodeTransactionHelper.validateTransactionCrypto(transactionData)
                && nodeTransactionHelper.validateBaseTransactionsDataIntegrity(transactionData) && validateTransactionSenderSignature(transactionData);
    }

    @Override
    public boolean validatePropagatedTransactionDataIntegrity(TransactionData transactionData) {
        return validateTransactionDataIntegrity(transactionData) && validateTransactionNodeSignature(transactionData) &&
                //validateTransactionTrustScore(transactionData) &&
                validateBaseTransactionAmounts(transactionData) && validatePot(transactionData);
    }

    @Override
    public boolean validateTransactionNodeSignature(TransactionData transactionData) {
        return transactionCrypto.verifySignature(transactionData);
    }

    @Override
    public boolean validateTransactionSenderSignature(TransactionData transactionData) {
        return EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial, TransactionType.EventHardFork).contains(transactionData.getType()) || transactionSenderCrypto.verifySignature(transactionData);
    }

    @Override
    public boolean validateTransactionTrustScore(TransactionData transactionData) {
        return nodeTransactionHelper.validateTrustScore(transactionData);
    }

    @Override
    public boolean validateBaseTransactionAmounts(TransactionData transactionData) {
        return nodeTransactionHelper.validateBaseTransactionAmounts(transactionData.getBaseTransactions());
    }

    @Override
    public boolean validateBalancesAndAddToPreBalance(TransactionData transactionData) {
        return nodeTransactionHelper.checkBalancesAndAddToPreBalance(transactionData);
    }

    @Override
    public boolean validateTokenMintingAndAddToAllocatedAmount(TransactionData transactionData) {
        return nodeTransactionHelper.checkTokenMintingAndAddToAllocatedAmount(transactionData);
    }

    @Override
    public boolean validateEventHardFork(TransactionData transactionData) {
        return nodeTransactionHelper.checkEventHardForkAndAddToEvents(transactionData);
    }

    @Override
    public Boolean validateFullNodeFeeDataIntegrity(TransactionData transactionData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean validateCurrencyUniquenessAndAddUnconfirmedRecord(TransactionData transactionData) {
        return nodeTransactionHelper.validateCurrencyUniquenessAndAddUnconfirmedRecord(transactionData);
    }

    @Override
    public <T extends BaseTransactionData & ITrustScoreNodeValidatable> boolean validateBaseTransactionTrustScoreNodeResult(T baseTransactionData) {
        return nodeTransactionHelper.validateBaseTransactionTrustScoreNodeResult(baseTransactionData);
    }

    @Override
    public boolean fullValidation(TransactionData transactionData) {
        return true;
    }


    @Override
    public boolean validatePot(TransactionData transactionData) {
        return EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial, TransactionType.EventHardFork).contains(transactionData.getType()) || potService.validatePot(transactionData);
    }

    @Override
    public boolean validateTransactionTimeFields(TransactionData transactionData) {
        return nodeTransactionHelper.validateTransactionTimeFields(transactionData);
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
