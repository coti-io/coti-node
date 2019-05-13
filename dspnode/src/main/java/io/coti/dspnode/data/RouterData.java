package io.coti.dspnode.data;

import io.coti.basenode.model.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

public enum RouterData {
    TRANSACTIONDATA("TransactionData"),
    ADDRESSDATA("AddressData");

    RouterData(final String dataCollectionName){
        this.dataCollectionName = dataCollectionName;
        dataCollection = RouterDataCollectionInjector.getCollectionByName(dataCollectionName);
    }

    private String dataCollectionName;
    private Collection dataCollection;

    public Collection getDataCollection(){
        return dataCollection;
    }

    @Component
    private static class RouterDataCollectionInjector {

        @Autowired
        private static ApplicationContext appContext;

        private static Collection getCollectionByName(String dataCollectionName){
            return (Collection)appContext.getBean(dataCollectionName);
        }
    }
}


