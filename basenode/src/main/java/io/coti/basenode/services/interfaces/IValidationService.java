package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;

public interface IValidationService {

    boolean validateSource(Hash hash);

    boolean validateAddress(Hash address);

    boolean validateTransactionDataIntegrity(TransactionData transactionData);

    boolean validatePropagatedTransactionDataIntegrity(TransactionData transactionData);

    boolean validateTransactionNodeSignature(TransactionData transactionData);

    boolean validateTransactionTrustScore(TransactionData transactionData);

    boolean validateBaseTransactionAmounts(TransactionData transactionData);

    boolean validateBalancesAndAddToPreBalance(TransactionData transactionData);

    <T extends BaseTransactionData & ITrustScoreNodeValidatable> boolean validateBaseTransactionTrustScoreNodeResult(T baseTransactionData);

    boolean partialValidation(TransactionData transactionData);

    boolean fullValidation(TransactionData transactionData);

    boolean validatePot(TransactionData transactionData);

    /**
     * Validates the Request signature and the current Cluster stamp state is OFF.
     * @param clusterStampPreparationData The ClusterStamp request.
     * @param currentClusterStampState The current node state in the cluster stamp flow.
     * @return true if request is valid and the state is correct. false otherwise.
     */
    boolean validateRequestAndOffState(ClusterStampStateData clusterStampPreparationData, ClusterStampState currentClusterStampState);

    /**
     * Validates the Request signature and the current Cluster stamp state is PREPARING.
     * @param nodeReadyForClusterStampData The ClusterStamp request.
     * @param currentClusterStampState The current node state in the cluster stamp flow.
     * @return true if request is valid and the state is correct. false otherwise.
     */
    boolean validateRequestAndPreparingState(ClusterStampStateData nodeReadyForClusterStampData, ClusterStampState currentClusterStampState);

    /**
     * Validates the Request signature and the current Cluster stamp state is READY.
     * @param clusterStampStateData The ClusterStamp request.
     * @param currentClusterStampState The current node state in the cluster stamp flow.
     * @return true if request is valid and the state is correct. false otherwise.
     */
    boolean validateRequestAndReadyState(ClusterStampStateData clusterStampStateData, ClusterStampState currentClusterStampState);

}