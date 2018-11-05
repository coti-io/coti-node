package io.coti.basenode.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.OutputBaseTransactionType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.data.interfaces.IBaseTransactionData;
import io.coti.basenode.services.interfaces.ITransactionTypeValidation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public enum TransactionTypeValidation implements ITransactionTypeValidation {
    Payment (TransactionType.Payment){
        @Override
        public boolean validateOutputBaseTransactions(TransactionData transactionData) {
          /*  List<IBaseTransactionData> outputBaseTransactions = transactionData.getOutputBaseTransactions();
            OutputBaseTransactionTypeValidation validation = OutputBaseTransactionTypeValidation.FullNodeFee;
            Class<? extends IBaseTransactionData> clazz = validation.getBaseTransactionClass();
            if(OutputBaseTransactionTypeValidation.FullNodeFee.getBaseTransactionClass().isInstance(outputBaseTransactions.get(0))){
                return true;
            } */
            return true;
        }
    },
    Transfer (TransactionType.Transfer) {
        @Override
        public boolean validateOutputBaseTransactions(TransactionData transactionData) {
       //     List<BaseTransactionData> outputBaseTransactions = transactionData.getOutputBaseTransactions();

            return true;
        }

    };
    private TransactionType type;
    TransactionTypeValidation(TransactionType type) {
        this.type = type;
    }

}
