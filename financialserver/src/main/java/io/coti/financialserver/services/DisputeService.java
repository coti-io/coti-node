
            transactionDisputesData = new TransactionDisputesData();
            transactionDisputesData.setHash(disputeData.getTransactionHash());
        }

        consumerDisputesData.appendDisputeHash(disputeData.getHash());
        merchantDisputes.put(merchantDisputesData);