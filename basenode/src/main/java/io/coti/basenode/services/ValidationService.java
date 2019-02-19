package io.coti.basenode.services;

import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IPotService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ValidationService implements IValidationService {
    @Autowired
    private Transactions transactions;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private IPotService potService;
    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;

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

        return CryptoHelper.IsAddressValid(address);
    }

    @Override
    public boolean validateTransactionDataIntegrity(TransactionData transactionData) {
        return transactionHelper.validateTransactionType(transactionData) && transactionHelper.validateTransactionCrypto(transactionData)
                && transactionHelper.validateBaseTransactionsDataIntegrity(transactionData);
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
    public boolean partialValidation(TransactionData transactionData) {
        return true;
    }

    @Override
    public boolean fullValidation(TransactionData transactionData) {
        return true;
    }


    @Override
    public boolean validatePot(TransactionData transactionData) {
        return potService.validatePot(transactionData);
    }

    @Override
    public boolean validatePrepareForClusterStampRequest(ClusterStampStateData clusterStampPreparationData, ClusterStampState clusterStampState) {
        return validateClusterStampRequestByState(clusterStampPreparationData,clusterStampState,ClusterStampState.OFF);
    }

    @Override
    public boolean validateReadyForClusterStampRequest(ClusterStampStateData nodeReadyForClusterStampData, ClusterStampState clusterStampState) {
        return validateClusterStampRequestByState(nodeReadyForClusterStampData,clusterStampState,ClusterStampState.READY);
    }

    private boolean validateClusterStampRequestByState(ClusterStampStateData clusterStampStateData, ClusterStampState currentState, ClusterStampState expectedState){
        if(currentState != expectedState){
            log.info("Expected cluster stamp state is already in progress");
            return false;
        }
        else if(!clusterStampStateCrypto.verifySignature(clusterStampStateData)){
            log.error("Wrong signature for \'prepare for cluster stamp\' request.");
            return false;
        }
        return true;
    }
}
